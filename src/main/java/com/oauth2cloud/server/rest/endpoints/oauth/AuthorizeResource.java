package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.api.LoginErrorCode;
import com.oauth2cloud.server.model.data.LoginEmailModel;
import com.oauth2cloud.server.model.data.LoginModel;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.util.CallLogUtil;
import com.oauth2cloud.server.rest.util.CookieUtil;
import com.oauth2cloud.server.rest.util.GoogleTokenValidator;
import com.oauth2cloud.server.rest.util.QueryUtil;
import io.swagger.annotations.*;
import org.codemonkey.simplejavamail.Mailer;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Level;

import static com.oauth2cloud.server.rest.util.EmailSender.sendTemplateEmail;
import static com.oauth2cloud.server.rest.util.OAuthUtil.badRequest;
import static com.oauth2cloud.server.rest.util.OAuthUtil.validateRequest;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Api("oauth2")
@CORSFilter.Skip
@NoXFrameOptionsFeature.NoXFrame
@Produces(MediaType.TEXT_HTML)
@Path("authorize")
public class AuthorizeResource extends BaseResource {
    private static final String INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES =
            "Invalid request. Please contact an administrator if this continues.";

    private static final String AUTHORIZE_TEMPLATE = "/templates/Authorize";

    /**
     * Helper method that takes a LoginCookie and expires it
     *
     * @param loginCookie to expire
     */
    private void expireLoginCookie(final LoginCookie loginCookie) {
        try {
            TXHelper.withinTransaction(em, () -> {
                loginCookie.setExpires(new Date());
                em.merge(loginCookie);
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to log user out, login cookie ID: " + loginCookie.getId(), e);
        }
    }

    /**
     * Validates the request parameters and shows a login screen
     *
     * @return a login screen
     */
    @ApiOperation(
            value = "Authorize User",
            notes = "Send users to this endpoint for the login page. Does not allow CORS nor to be embedded in an iframe"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns 200 if request is well formed"),
            @ApiResponse(code = 400, message = "Returns 400 if any of the required parameters are missing or invalid")
    })
    @GET
    public Response auth(
            @ApiParam(
                    value = "The desired response type from the API, either code or token",
                    required = true, allowableValues = "code, token"
            )
            @QueryParam("response_type") final String responseType,
            @ApiParam(value = "The identifier for the client for which the user will be authenticated", required = true)
            @QueryParam("client_id") final String clientId,
            @ApiParam(value = "The redirect URI that the user will be sent to after successfully or unsuccessfully authorizing", required = true)
            @QueryParam("redirect_uri") final String redirectUri,
            @ApiParam(value = "A state variable from the client that can be used to validate that the redirect came from the OAuth2 server. The state will be included in the redirect URI in the query parameter for the code response type and the hash for the token response type")
            @QueryParam("state") final String state,
            @ApiParam(value = "The space delimited list of scopes that the client is requesting")
            @QueryParam("scope") final String scope,
            @ApiParam(hidden = true)
            @QueryParam("error_code") final String errorCode,
            @ApiParam(value = "Pass true to require the user to re-authenticate if they are already logged in", required = false, defaultValue = "false", allowableValues = "true,false")
            @QueryParam("logout") final boolean logout
    ) {
        final Response error = validateRequest(em, responseType, clientId, redirectUri, scope);
        if (error != null) {
            return error;
        }

        final Client client = QueryUtil.getClient(em, clientId);
        CallLogUtil.logCall(em, client, req);

        final LoginCookie loginCookie = CookieUtil.getLoginCookie(em, req, client);
        // if the user is already logged in
        if (loginCookie != null) {
            if (logout) {
                // forcing a log out, so expire the login cookie
                expireLoginCookie(loginCookie);
            } else {
                // return the handler for a successful login
                final LoginCode loginCode = makeLoginCode(
                        em,
                        loginCookie.getUser(), client, scope, redirectUri,
                        responseType, state, false
                );
                return loginCodeRedirect(loginCode);
            }
        }

        final LoginModel loginModel = new LoginModel(client, fromString(errorCode), false);

        return Response.ok(new Viewable(AUTHORIZE_TEMPLATE, loginModel)).build();
    }

    private static LoginErrorCode fromString(String errorCode) {
        if (isBlank(errorCode)) {
            return null;
        }
        try {
            return LoginErrorCode.valueOf(errorCode);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to parse error code", e);
            return null;
        }
    }

    private Response loginCodeRedirect(final LoginCode loginCode) {
        return Response.status(Response.Status.FOUND).location(
                req.getUriInfo().getBaseUriBuilder()
                        .path("login").path(loginCode.getToken())
                        .build()
        ).build();
    }

    /**
     * Processes either a login attempt or granted permissions, doing the appropriate redirect on success
     *
     * @return a Viewable if further action is required
     */
    @ApiOperation(value = "Complete Login", hidden = true)
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response doAuthorize(
            @QueryParam("response_type") final String responseType,
            @QueryParam("client_id") final String clientId,
            @QueryParam("redirect_uri") final String redirectUri,
            @QueryParam("state") final String state,
            @QueryParam("scope") final String scope,
            @QueryParam("logout") final boolean logout,
            // form parameters
            @FormParam("action") final String action,
            @FormParam("remember_me") final String rememberMe,
            @FormParam("google_token") final String googleToken,
            @FormParam("email") final String email
    ) {
        // validate the client id stuff again
        final Response badRequest = validateRequest(em, responseType, clientId, redirectUri, scope);
        if (badRequest != null) {
            return badRequest;
        }

        if (isBlank(action)) {
            return badRequest(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
        }

        final Client client = QueryUtil.getClient(em, clientId);
        CallLogUtil.logCall(em, client, req);

        final boolean remember = "on".equalsIgnoreCase(rememberMe);

        // this resource is used for a few different actions which are represented as hidden inputs in the forms
        // this is done so that the query string can be preserved across all requests without any special work
        switch (action) {
            // handle the login action
            case "email": {
                if (isBlank(email)) {
                    return badRequest("Invalid e-mail address");
                }

                final User user = QueryUtil.findOrCreateUser(em, client.getApplication(), email);
                if (user == null) {
                    return badRequest("Failed to find or create user with provided e-mail address");
                }

                sendLoginCode(
                        makeLoginCode(
                                em,
                                user, client, scope, redirectUri,
                                responseType, state, remember
                        )
                );


                final LoginModel loginModel = new LoginModel(client, null, true);

                return Response.ok(new Viewable(AUTHORIZE_TEMPLATE, loginModel)).build();
            }

            case "google": {
                try {
                    final User user = doGoogleLogin(client.getApplication(), googleToken);

                    final LoginCode loginCode = makeLoginCode(
                            em,
                            user, client, scope, redirectUri,
                            responseType, state, remember
                    );

                    return loginCodeRedirect(loginCode);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE,
                            String.format("Failed to use google to log in for application ID %s",
                                    client.getApplication().getId()),
                            e);
                    return badRequest(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
                }
            }
            default:
                return badRequest(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
        }
    }


    @Inject
    private Mailer mailer;

    @Inject
    private freemarker.template.Configuration configuration;

    private boolean sendLoginCode(final LoginCode loginCode) {
        sendTemplateEmail(
                mailer, configuration,
                loginCode.getClient().getApplication().getSupportEmail(),
                loginCode.getUser().getEmail(),
                String.format("Log In to %s - %s",
                        loginCode.getClient().getName(), loginCode.getClient().getApplication().getName()),
                "LoginEmail.ftl",
                new LoginEmailModel(loginCode)
        );

        return true;
    }

    @Inject
    private GoogleTokenValidator googleTokenValidator;

    /**
     * Use a google token to log in as a google user
     *
     * @param application application checking token
     * @param googleToken token from google
     * @return User if successfully logged in
     */
    private User doGoogleLogin(final Application application, final String googleToken) {
        final String email = googleTokenValidator.getTokenEmail(application.getGoogleCredentials(), googleToken);

        return QueryUtil.findOrCreateUser(em, application, email);
    }

    private LoginCode makeLoginCode(
            final EntityManager em,
            final User user, final Client client, final String scope, final String redirectUri,
            final String responseType, final String state, final boolean rememberMe
    ) {
        final LoginCode loginCode = new LoginCode();

        loginCode.setUser(user);
        loginCode.setClient(client);
        loginCode.setScope(scope);
        loginCode.setRedirectUri(redirectUri);
        loginCode.setResponseType(ResponseType.valueOf(responseType));
        loginCode.setState(state);
        loginCode.setRememberMe(rememberMe);
        loginCode.setBaseUri(req.getUriInfo().getBaseUri().toString());

        loginCode.setToken(randomAlphanumeric(96));
        loginCode.setExpires(
                client.getLoginCodeTtl() != null ?
                        new Date(System.currentTimeMillis() + (client.getLoginCodeTtl() * 1000L)) :
                        null
        );
        loginCode.setUsed(false);

        try {
            return TXHelper.withinTransaction(em, () -> em.merge(loginCode));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create login code", e);
            return null;
        }
    }
}
