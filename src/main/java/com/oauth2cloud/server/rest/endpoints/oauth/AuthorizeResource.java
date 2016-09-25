package com.oauth2cloud.server.rest.endpoints.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.hibernate.util.QueryUtil;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.data.LoginModel;
import com.oauth2cloud.server.model.data.PermissionsModel;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@NoXFrameOptionsFeature.NoXFrame
@Produces(MediaType.TEXT_HTML)
@Path(OAuth2Application.OAUTH_PATH + "/authorize")
public class AuthorizeResource extends OAuthResource {
    public static final String TOKEN = "token",
            CODE = "code",
            SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN = "Something went wrong. Please try again.",
            YOUR_LOGIN_ATTEMPT_HAS_EXPIRED_PLEASE_TRY_AGAIN = "Your login attempt has expired. Please try again.",
            INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES = "Invalid request. Please contact an administrator if this continues.";

    public static final String EMAIL_LOGIN_ACTION = "email-login";
    public static final String GOOGLE_LOGIN_ACTION = "google-login";
    public static final String PERMISSIONS_ACTION = "permissions";

    @QueryParam("response_type")
    private String responseType;
    @QueryParam("client_id")
    private String clientId;
    @QueryParam("redirect_uri")
    private String redirectUri;
    @QueryParam("state")
    private String state;
    @QueryParam("scope")
    private Set<String> scopes;

    /**
     * An extra query parameter that can be passed on to the GET request to log the user out if they are already logged in
     */
    @QueryParam("logout")
    private boolean logout;

    /**
     * Performing a GET on this path returns a 204 and logs the user out if the user is logged in
     *
     * @return 204 response
     */
    @GET
    @Path("logout")
    public Response logout() {
        if (clientId == null) {
            return error("'client_id' is a required query parameter to log out.");
        }

        final Client client = QueryUtil.getClient(em, clientId);
        if (client == null) {
            return error("Invalid client ID.");
        }

        QueryUtil.logCall(em, client, containerRequestContext);

        final LoginCookie loginCookie = getLoginCookie(client);
        expireLoginCookie(loginCookie);

        return Response.noContent().build();
    }

    private void expireLoginCookie(final LoginCookie loginCookie) {
        if (loginCookie == null) {
            return;
        }
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
     * This method validates all the query parameters and returns an error if anything is wrong with the authorization
     * request
     *
     * @param responseType either code or token
     * @param clientId     valid id of a client
     * @param redirectUri  a URI that the client is allowed to redirect to
     * @param scopes       a list of scopes that the client is requesting
     * @return an error if anything is wrong with the aforementioned parameters, otherwise null
     */
    private Response validateRequest(final String responseType, final String clientId, final String redirectUri, final Set<String> scopes) {
        // verify all the query parameters are passed
        if (isBlank(clientId) || isBlank(redirectUri) || isBlank(responseType)) {
            return error("Client ID, redirect URI, and response type are all required to log in.");
        }

        if (!TOKEN.equalsIgnoreCase(responseType) && !CODE.equalsIgnoreCase(responseType)) {
            return error("Invalid response type. Must be one of 'token' or 'code'");
        }

        // verify redirect URL is a proper redirect URL
        final URI toRedirect;
        try {
            toRedirect = new URI(redirectUri);
        } catch (Exception e) {
            return error("Invalid redirect URL: " + e.getMessage());
        }

        // first look up the Client by the client identifier
        final Client client = QueryUtil.getClient(em, clientId);
        if (client == null) {
            return error("Client ID not found.");
        }

        // verify the redirect uri is in the list of the client's allowed redirect uris
        boolean validRedirect = false;
        for (final String uri : client.getUris()) {
            try {
                final URI cUri = new URI(uri);
                // scheme, host, and port must match
                if (partialMatch(cUri, toRedirect)) {
                    validRedirect = true;
                    break;
                }
            } catch (Exception e) {
                return error("The client has an invalid redirect URI registered: " + uri + "; " + e.getMessage());
            }
        }
        if (!validRedirect) {
            return error("The redirect URI " + toRedirect.toString() + " is not allowed for this client.");
        }

        if (TOKEN.equalsIgnoreCase(responseType)) {
            if (!client.getFlows().contains(GrantFlow.IMPLICIT)) {
                return error("This client does not support the implicit grant flow.");
            }
        }

        if (CODE.equalsIgnoreCase(responseType)) {
            if (!client.getFlows().contains(GrantFlow.CODE)) {
                return error("This client does not support the code grant flow.");
            }
        }

        // verify all the requested scopes are available to the client
        if (scopes != null && scopes.size() > 0) {
            final Set<String> scopeNames = client.getScopes().stream()
                    .map(ClientScope::getScope)
                    .map(Scope::getName)
                    .collect(Collectors.toSet());

            if (!scopeNames.containsAll(scopes)) {
                final String joinedScopes = scopes.stream()
                        .filter((s) -> !scopeNames.contains(s))
                        .collect(Collectors.joining(", "));
                return error(String.format("The following scopes are requested but not allowed for this client: %s", joinedScopes));
            }
        }

        return null;
    }

    /**
     * Validates the request parameters and shows a login screen
     *
     * @return a login screen
     */
    @GET
    @CORSFilter.Skip
    public Response auth() {
        final Response error = validateRequest(responseType, clientId, redirectUri, scopes);
        if (error != null) {
            return error;
        }

        final Client client = QueryUtil.getClient(em, clientId);
        QueryUtil.logCall(em, client, containerRequestContext);

        final LoginCookie loginCookie = getLoginCookie(client);
        if (loginCookie != null) {
            if (logout) {
                expireLoginCookie(loginCookie);
            } else {
                return getSuccessfulLoginResponse(
                        loginCookie.getUser(),
                        client,
                        scopes,
                        redirectUri,
                        responseType,
                        state,
                        loginCookie.isRememberMe()
                );
            }
        }

        final LoginModel lrm = new LoginModel();
        lrm.setClient(client);
        lrm.setRedirectUri(redirectUri);
        lrm.setState(state);
        lrm.setURLs(containerRequestContext);

        return Response.ok(new Viewable("/templates/Authorize", lrm)).build();
    }


    /**
     * Processes either a login attempt or granted permissions, doing the appropriate redirect on success
     *
     * @param formParams all the parameters posted to the form
     * @return a Viewable if further action is required
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @CORSFilter.Skip
    public Response login(MultivaluedMap<String, String> formParams) {
        // validate the client id stuff again
        final Response error = validateRequest(responseType, clientId, redirectUri, scopes);
        if (error != null) {
            return error;
        }

        if (formParams == null) {
            return error(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
        }

        final String action = formParams.getFirst("action");
        if (isBlank(action)) {
            return error(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
        }

        boolean rememberMe = "on".equalsIgnoreCase(formParams.getFirst("rememberMe"));

        final LoginModel lrm = new LoginModel();
        lrm.setURLs(containerRequestContext);
        lrm.setState(state);
        lrm.setRedirectUri(redirectUri);
        final Client client = QueryUtil.getClient(em, clientId);
        lrm.setClient(client);

        QueryUtil.logCall(em, client, containerRequestContext);

        // this resource is used for a few different actions which are represented as hidden inputs in the forms
        // this is done so that the query string can be preserved across all requests without any special work
        switch (action) {
            // handle the login action
            case EMAIL_LOGIN_ACTION:
                final String email = formParams.getFirst("email");

                return getSuccessfulLoginResponse(
                        QueryUtil.findOrCreateUser(em, client.getApplication(), email),
                        client, scopes, redirectUri, responseType, state, rememberMe
                );

            case GOOGLE_LOGIN_ACTION:
                final String googleToken = formParams.getFirst("googleToken");
                return getSuccessfulLoginResponse(
                        doGoogleLogin(client.getApplication(), googleToken),
                        client, scopes, redirectUri, responseType, state, rememberMe
                );

            // handle the permissions action
            case PERMISSIONS_ACTION:
                final String loginToken = formParams.getFirst("login_token");

                // they just completed the second step of the login
                if (loginToken != null) {
                    final Token token = QueryUtil.getPermissionToken(em, loginToken, client);
                    if (token == null) {
                        lrm.setLoginError(SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
                    } else {
                        if (token.getExpires().before(new Date())) {
                            lrm.setLoginError(YOUR_LOGIN_ATTEMPT_HAS_EXPIRED_PLEASE_TRY_AGAIN);
                        } else {
                            // first get all the client scopes we will try to approve or check if are approved
                            final Set<ClientScope> clientScopes = QueryUtil.getScopes(em, client, scopes);
                            // we'll populate this as we loop through the scopes
                            final Set<AcceptedScope> tokenScopes = new HashSet<>();
                            // get all the scope ids that were explicitly granted
                            final Set<UUID> acceptedScopeIds = formParams.keySet().stream().map((s) -> {
                                try {
                                    return s != null && s.startsWith("SCOPE") &&
                                            "on".equalsIgnoreCase(formParams.getFirst(s)) ?
                                            UUID.fromString(s.substring("SCOPE".length())) : null;
                                } catch (Exception e) {
                                    return null;
                                }
                            }).filter((i) -> i != null).collect(Collectors.toSet());

                            // if it's not ASK, or it's explicitly granted, we should create/find the AcceptedScope record
                            // create/find the accepted scope for this client scope
                            tokenScopes.addAll(
                                    clientScopes.stream()
                                            .filter(cs -> !cs.getPriority().equals(ClientScope.Priority.ASK) || acceptedScopeIds.contains(cs.getScope().getId()))
                                            .map(cs -> QueryUtil.acceptScope(em, token.getUser(), cs))
                                            .collect(Collectors.toList())
                            );

                            final Token.Type type = getTokenType(responseType);
                            // now create the token we will be returning to the user
                            return getRedirectResponse(redirectUri, state, generateToken(type, token, tokenScopes), rememberMe);
                        }
                    }
                } else {
                    return error(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
                }
                return Response.ok(new Viewable("/templates/Authorize", lrm)).build();
            default:
                return error(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
        }
    }

    private static final WebTarget GOOGLE_TOKEN_VALIDATE = ClientBuilder.newClient()
            .target("https://www.googleapis.com/oauth2/v1/tokeninfo");

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

    private Response getSuccessfulLoginResponse(final User user, final Client client, final Set<String> scopes, final String redirectUri,
                                                final String responseType, final String state, final boolean rememberMe) {
        // successfully authenticated the user
        final Set<ClientScope> toAsk = QueryUtil.getScopesToRequest(em, client, user, scopes);
        if (!toAsk.isEmpty()) {
            // we need to generate a temporary token for them to get to the next step with
            final Token token = QueryUtil.generatePermissionToken(em, user, client, redirectUri);
            final PermissionsModel permissionsModel = new PermissionsModel(token, toAsk, rememberMe);
            permissionsModel.setClient(client);
            permissionsModel.setURLs(containerRequestContext);
            permissionsModel.setState(state);
            permissionsModel.setRedirectUri(redirectUri);
            return Response.ok(new Viewable("/templates/Permissions", permissionsModel)).build();
        } else {
            // accept all the always permissions
            final Set<ClientScope> clientScopes = QueryUtil.getScopes(em, client, scopes);
            final Set<AcceptedScope> acceptedScopes = clientScopes.stream()
                    .map(clientScope -> QueryUtil.acceptScope(em, user, clientScope))
                    .collect(Collectors.toSet());

            final Token.Type type = getTokenType(responseType);
            // redirect with token since they've already asked for all the permissions
            final Token token = QueryUtil.generateToken(em, type, client, user, getExpires(client, type),
                    redirectUri, acceptedScopes, null, null);
            return getRedirectResponse(redirectUri, state, token, rememberMe);
        }
    }

    /**
     * Map the requested response type to the internal token type
     *
     * @param responseType response type, either code or token
     * @return the Token.Type that should be generated by the flow
     */
    private Token.Type getTokenType(String responseType) {
        return CODE.equalsIgnoreCase(responseType) ? Token.Type.CODE : Token.Type.ACCESS;
    }

    /**
     * Does the appropriate redirect based on the passed redirect URI and the token, always appending the state
     *
     * @param redirectUri URI passed by the client to redirect to
     * @param state       state of the client upon sending to the authorize endpoint, returned in the redirect url
     * @param token       access or code token that was generated, depending on the type
     */
    private Response getRedirectResponse(
            final String redirectUri,
            final String state,
            final Token token,
            final boolean rememberMe
    ) {
        final UriBuilder toRedirect = UriBuilder.fromUri(redirectUri);

        final String scope = token.getAcceptedScopes().stream()
                .map(AcceptedScope::getClientScope).map(ClientScope::getScope).map(Scope::getName)
                .collect(Collectors.joining(" "));
        final String expiresIn = Long.toString((token.getExpires().getTime() - System.currentTimeMillis()) / 1000L);

        if (token.getType().equals(Token.Type.ACCESS)) {
            final MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
            params.putSingle("access_token", token.getToken());
            params.putSingle("token_type", TokenResponse.BEARER);
            if (state != null) {
                params.putSingle("state", state);
            }
            params.putSingle("expires_in", expiresIn);
            params.putSingle("scope", scope);

            toRedirect.fragment(mapToQueryString(params));
        }

        if (token.getType().equals(Token.Type.CODE)) {
            toRedirect.queryParam("code", token.getToken());
            if (state != null) {
                toRedirect.queryParam("state", state);
            }
        }

        return Response.status(Response.Status.FOUND)
                .location(toRedirect.build())
                .cookie(getNewCookie(token, rememberMe))
                .build();
    }

    @HeaderParam("X-Forwarded-Proto")
    private String forwardedProto;

    private static final String IPV4_ADDRESS_REGEX = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    private static final String HTTPS = "HTTPS";
    private static final String OAUTH2_CLOUD_LOGIN_COOKIE = "OAuth2 Cloud Login Cookie";


    private NewCookie getNewCookie(Token tkn, Boolean rememberMe) {
        Date expires;
        LoginCookie loginCookie = getLoginCookie(tkn.getClient());
        if (loginCookie != null) {
            // we should re-use the same values
            expires = loginCookie.getExpires();
        } else {
            expires = new Date(System.currentTimeMillis() + ONE_MONTH);
            // we should issue a new cookie
            loginCookie = makeLoginCookie(tkn.getUser(), RandomStringUtils.randomAlphanumeric(64), expires, rememberMe);
        }

        int maxAge = rememberMe ? (new Long(ONE_MONTH / 1000L)).intValue() : NewCookie.DEFAULT_MAX_AGE;
        Date expiry = rememberMe ? expires : null;

        boolean isHTTPS = HTTPS.equalsIgnoreCase(forwardedProto);

        String domain = containerRequestContext.getUriInfo().getBaseUri().getHost();
        if (domain != null) {
            if (domain.matches(IPV4_ADDRESS_REGEX)) {
                // don't put a domain on a cookie that is passed to an IP address
                domain = null;
            } else {
                // the domain should be the last two pieces of the domain name
                String[] pieces = domain.split("\\.");
                List<String> pcs = Arrays.asList(pieces);
                domain = pcs.subList(Math.max(0, pcs.size() - 2), pcs.size()).stream().collect(Collectors.joining("."));
            }
        }

        return new NewCookie(getCookieName(tkn.getClient()), loginCookie.getSecret(), "/", domain, NewCookie.DEFAULT_VERSION,
                OAUTH2_CLOUD_LOGIN_COOKIE, maxAge, expiry, isHTTPS, true);
    }

    private LoginCookie makeLoginCookie(User user, String secret, Date expires, boolean rememberMe) {
        final LoginCookie loginCookie = new LoginCookie();
        loginCookie.setUser(user);
        loginCookie.setSecret(secret);
        loginCookie.setExpires(expires);
        loginCookie.setRememberMe(rememberMe);

        try {
            TXHelper.withinTransaction(em, () -> {
                em.persist(loginCookie);
                em.flush();
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create a login cookie", e);
            return null;
        }

        return loginCookie;
    }

    /**
     * Helper function to generate a token from a permission token
     *
     * @param type            type of token to generate
     * @param permissionToken the token that triggered the generation
     * @param scopes          the list of scopes for the token
     * @return generated token
     */
    private Token generateToken(Token.Type type, Token permissionToken, Set<AcceptedScope> scopes) {
        return QueryUtil.generateToken(em, type, permissionToken.getClient(), permissionToken.getUser(),
                getExpires(permissionToken.getClient(), type), permissionToken.getRedirectUri(), scopes, null, null);
    }

}
