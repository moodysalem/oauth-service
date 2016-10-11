package com.oauth2cloud.server.rest.endpoints.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.api.LoginErrorCode;
import com.oauth2cloud.server.model.data.LoginEmailModel;
import com.oauth2cloud.server.model.data.LoginModel;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.util.CallLogUtil;
import com.oauth2cloud.server.rest.util.CookieUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;
import org.codemonkey.simplejavamail.Mailer;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Level;

import static com.oauth2cloud.server.rest.util.EmailSender.sendTemplateEmail;
import static com.oauth2cloud.server.rest.util.OAuthUtil.badRequest;
import static com.oauth2cloud.server.rest.util.OAuthUtil.validateRequest;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

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
    @GET
    public Response auth(
            @QueryParam("response_type") final String responseType,
            @QueryParam("client_id") final String clientId,
            @QueryParam("redirect_uri") final String redirectUri,
            @QueryParam("state") final String state,
            @QueryParam("scope") final String scope,
            @QueryParam("error_code") final String errorCode,
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
                        .path("login").path(loginCode.getCode())
                        .build()
        ).build();
    }

    /**
     * Processes either a login attempt or granted permissions, doing the appropriate redirect on success
     *
     * @return a Viewable if further action is required
     */
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

        final boolean remember = "on".equals(rememberMe);

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
                final User user = doGoogleLogin(client.getApplication(), googleToken);
                if (user == null) {
                    return badRequest("Invalid google token");
                }

                final LoginCode loginCode = makeLoginCode(
                        em,
                        user, client, scope, redirectUri,
                        responseType, state, remember
                );
                return loginCodeRedirect(loginCode);
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

    private static final WebTarget GOOGLE_TOKEN_VALIDATE = ClientBuilder.newClient()
            .target("https://www.googleapis.com/oauth2/v1/tokeninfo");

    /**
     * Use a google token to log in as a google user
     *
     * @param application application to log in
     * @param googleToken token from google
     * @return User if successfully logged in
     */
    private User doGoogleLogin(final Application application, final String googleToken) {
        if (application.getGoogleCredentials() == null) {
            throw new IllegalArgumentException(
                    String.format("The application %s is not fully configured for Google Login.", application.getName()));
        }

        if (isBlank(googleToken)) {
            throw new IllegalArgumentException("Invalid Google Token supplied.");
        }

        final Response tokenInfo = GOOGLE_TOKEN_VALIDATE
                .queryParam("access_token", googleToken)
                .request(MediaType.APPLICATION_JSON).get();

        if (tokenInfo.getStatus() != 200) {
            throw new IllegalArgumentException("Invalid Google Token supplied.");
        }
        final JsonNode tokenInfoJson = tokenInfo.readEntity(JsonNode.class);

        final JsonNode aud = tokenInfoJson.get("audience");
        if (aud == null || !aud.asText().equals(application.getGoogleCredentials().getId())) {
            throw new IllegalArgumentException("Token supplied is not for the correct client.");
        }

        final JsonNode emailVerified = tokenInfoJson.get("verified_email");
        if (emailVerified == null || !emailVerified.asBoolean()) {
            throw new IllegalArgumentException("Google E-mail address is not yet verified.");
        }

        final JsonNode scopes = tokenInfoJson.get("scope");
        final String scope = scopes == null ? null : scopes.asText().trim();
        if (isEmpty(scope)) {
            throw new IllegalArgumentException("The profile and e-mail scopes are required to log in.");
        }

        final Response userInfo = ClientBuilder.newClient().target("https://www.googleapis.com/plus/v1/people/me")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + googleToken)
                .get();

        if (userInfo.getStatus() != 200) {
            throw new IllegalArgumentException("Failed to get user info.");
        }

        final JsonNode userData = userInfo.readEntity(JsonNode.class);

        final JsonNode emails = userData.get("emails");
        if (emails == null || !emails.isArray() || emails.size() != 1) {
            throw new IllegalArgumentException("Google account is not associated with an e-mail or has more than one e-mail.");
        }
        final String email = emails.get(0).get("value").asText();

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

        loginCode.setCode(randomAlphanumeric(96));
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
