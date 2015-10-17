package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.*;
import com.leaguekit.util.RandomStringUtil;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Path("authorize")
@Produces(MediaType.TEXT_HTML)
public class AuthorizeResource extends BaseResource {

    public static final String TOKEN = "token";
    public static final String CODE = "code";
    public static final String INVALID_E_MAIL_OR_PASSWORD = "Invalid e-mail or password.";
    public static final String SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN = "Something went wrong. Please try again.";
    public static final String YOUR_LOGIN_ATTEMPT_HAS_EXPIRED_PLEASE_TRY_AGAIN = "Your login attempt has expired. Please try again.";

    @QueryParam("response_type")
    String responseType;
    @QueryParam("client_id")
    String clientId;
    @QueryParam("redirect_uri")
    String redirectUri;
    @QueryParam("state")
    String state;
    @QueryParam("scope")
    List<String> scopes;

    /**
     * An extra query parameter that can be passed on to the GET request to log the user out
     */
    @QueryParam("logout")
    boolean logout;

    // so we don't rerun the getErrorResponse logic for a valid request, just store whether it's valid in this variable
    private boolean valid = false;

    @GET
    @Path("logout")
    public Response logout() {
        if (clientId == null) {
            return error("Client ID is a required query parameter to log out.");
        }

        Client c = getClient(clientId);
        if (c == null) {
            return error("Invalid client ID.");
        }

        LoginCookie loginCookie = getLoginCookie(c);
        deleteLoginCookie(loginCookie);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private void deleteLoginCookie(LoginCookie loginCookie) {
        if (loginCookie == null) {
            return;
        }
        loginCookie.setExpires(new Date());
        try {
            beginTransaction();
            em.merge(loginCookie);
            commit();
        } catch (Exception e) {
            rollback();
            LOG.log(Level.SEVERE, "Failed to log user out, LCID: " + loginCookie.getId(), e);
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
     * @return an error if anything is wrong with the aforementioned parameters
     */
    private Response getErrorResponse(String responseType, String clientId, String redirectUri, List<String> scopes) {
        if (valid) {
            return null;
        }
        // verify all the query parameters are passed
        if (clientId == null || redirectUri == null || responseType == null) {
            return error("Client ID, redirect URI, and response type are all required for this endpoint.");
        }
        if (!TOKEN.equalsIgnoreCase(responseType) && !CODE.equalsIgnoreCase(responseType)) {
            return error("Invalid response type. Must be one of 'token' or 'code'");
        }

        // verify redirect URL is a proper redirect URL
        URI toRedirect;
        try {
            toRedirect = new URI(redirectUri);
        } catch (Exception e) {
            return error("Invalid redirect URL: " + e.getMessage());
        }

        // first look up the Client by the client identifier
        Client c = getClient(clientId);
        if (c == null) {
            return error("Client ID not found.");
        }

        // verify the redirect uri is in the list of the client's allowed redirect uris
        boolean validRedirect = false;
        for (String uri : c.getUris()) {
            try {
                URI cUri = new URI(uri);
                // scheme, host, and port must match
                if (cUri.getScheme().equalsIgnoreCase(toRedirect.getScheme()) &&
                    cUri.getHost().equalsIgnoreCase(toRedirect.getHost()) &&
                    cUri.getPort() == toRedirect.getPort()) {
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
            if (!c.getFlows().contains(Client.GrantFlow.IMPLICIT)) {
                return error("This client does not support the implicit grant flow.");
            }
        }
        if (CODE.equalsIgnoreCase(responseType)) {
            if (!c.getFlows().contains(Client.GrantFlow.CODE)) {
                return error("This client does not support the code grant flow.");
            }
        }

        // verify all the requested scopes are available to the client
        if (scopes != null && scopes.size() > 0) {
            Set<String> scopeNames = c.getClientScopes().stream()
                .map(ClientScope::getScope).map(Scope::getName).collect(Collectors.toSet());
            if (!scopeNames.containsAll(scopes)) {
                String joinedScopes = scopes.stream().filter((s) -> !scopeNames.contains(s)).collect(Collectors.joining(", "));
                return error("The following scopes are requested but not allowed for this client: " + joinedScopes);
            }
        }

        valid = true;
        return null;
    }

    public static class AuthorizeModel {
        private Client client;
        private String loginError;

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }

        public String getLoginError() {
            return loginError;
        }

        public void setLoginError(String loginError) {
            this.loginError = loginError;
        }
    }

    public static class PermissionsModel {
        private Client client;
        private Token token;
        private List<ClientScope> clientScopes;
        private boolean rememberMe;

        public Token getToken() {
            return token;
        }

        public void setToken(Token token) {
            this.token = token;
        }

        public List<ClientScope> getClientScopes() {
            return clientScopes;
        }

        public void setClientScopes(List<ClientScope> clientScopes) {
            this.clientScopes = clientScopes;
        }

        public boolean isRememberMe() {
            return rememberMe;
        }

        public void setRememberMe(boolean rememberMe) {
            this.rememberMe = rememberMe;
        }

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }
    }

    /**
     * Validates the request parameters and shows a login screen
     *
     * @return a login screen
     */
    @GET
    public Response auth() {
        Response error = getErrorResponse(responseType, clientId, redirectUri, scopes);
        if (error != null) {
            return error;
        }

        Client client = getClient(clientId);

        LoginCookie loginCookie = getLoginCookie(client);
        if (loginCookie != null) {
            if (logout) {
                deleteLoginCookie(loginCookie);
            } else {
                return getSuccessfulLoginResponse(loginCookie.getUser(), client, scopes, redirectUri, responseType, state,
                    loginCookie.isRememberMe());
            }
        }

        AuthorizeModel ar = new AuthorizeModel();
        ar.setClient(client);

        return Response.ok(new Viewable("/templates/Login", ar)).build();
    }


    /**
     * Processes either a login attempt or granted permissions, doing the appropriate redirect on success
     *
     * @param formParams all the parameters posted to the form
     * @return a Viewable if further action is required
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(MultivaluedMap<String, String> formParams) {
        String email = formParams.getFirst("email");
        String password = formParams.getFirst("password");
        boolean rememberMe = "on".equalsIgnoreCase(formParams.getFirst("rememberMe"));

        String loginToken = formParams.getFirst("login_token");
        // validate the client id stuff again
        Response error = getErrorResponse(responseType, clientId, redirectUri, scopes);
        if (error != null) {
            return error;
        }

        AuthorizeModel ar = new AuthorizeModel();
        Client client = getClient(clientId);
        ar.setClient(client);

        // they just completed the second step of the login
        if (loginToken != null) {
            Token t = getPermissionToken(loginToken, client);
            if (t == null) {
                ar.setLoginError(SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
            } else {
                if (t.getExpires().before(new Date())) {
                    ar.setLoginError(YOUR_LOGIN_ATTEMPT_HAS_EXPIRED_PLEASE_TRY_AGAIN);
                } else {
                    // first get all the client scopes we will try to approve or check if are approved
                    List<ClientScope> clientScopes = getScopes(client, scopes);
                    // we'll populate this as we loop through the scopes
                    List<AcceptedScope> tokenScopes = new ArrayList<>();
                    // get all the scope ids that were explicitly granted
                    Set<Long> acceptedScopeIds = formParams.keySet().stream().map((s) -> {
                        try {
                            return s != null && s.startsWith("SCOPE") &&
                                "on".equalsIgnoreCase(formParams.getFirst(s)) ?
                                Long.parseLong(s.substring("SCOPE".length())) : null;
                        } catch (Exception e) {
                            return null;
                        }
                    }).filter((i) -> i != null).collect(Collectors.toSet());
                    for (ClientScope cs : clientScopes) {
                        // if it's not ASK, or it's explicitly granted, we should create/find the AcceptedScope record
                        if (!cs.getPriority().equals(ClientScope.Priority.ASK) || acceptedScopeIds.contains(cs.getScope().getId())) {
                            // create/find the accepted scope for this client scope
                            tokenScopes.add(acceptScope(t.getUser(), cs));
                        }
                    }
                    Token.Type type = getTokenType(responseType);
                    // now create the token we will be returning to the user
                    Token token = generateToken(type, t, tokenScopes);
                    return getRedirectResponse(redirectUri, state, token, rememberMe);
                }
            }
        } else {
            // validate the username and password
            if (email != null && password != null) {
                long t1 = System.currentTimeMillis();
                boolean success = false;
                User user = getUser(email, client.getApplication().getId());
                if (user == null) {
                    ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
                } else {
                    if (user.getPassword() == null) {
                        ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
                    } else {
                        if (BCrypt.checkpw(password, user.getPassword())) {
                            success = true;
                        } else {
                            ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
                        }
                    }
                }
                long t2 = System.currentTimeMillis();
                if (t2 - t1 < THREE_SECONDS) {
                    try {
                        Thread.sleep(THREE_SECONDS - (t2 - t1));
                    } catch (InterruptedException e) {
                        LOG.log(Level.SEVERE, "Thread sleep interrupted", e);
                    }
                }
                if (success) {
                    return getSuccessfulLoginResponse(user, client, scopes, redirectUri, responseType, state, rememberMe);
                }
            } else {
                ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
            }
        }

        return Response.ok(new Viewable("/templates/Login", ar)).build();
    }

    private Response getSuccessfulLoginResponse(User user, Client client, List<String> scopes, String redirectUri,
                                                String responseType, String state, boolean rememberMe) {
        // successfully authenticated the user
        List<ClientScope> toAsk = getScopesToRequest(client, user, scopes);
        if (toAsk.size() > 0) {
            // we need to generate a temporary token for them to get to the next step with
            Token t = generatePermissionToken(user, client, redirectUri);
            PermissionsModel pr = new PermissionsModel();
            pr.setClientScopes(toAsk);
            pr.setToken(t);
            pr.setRememberMe(rememberMe);
            pr.setClient(client);
            return Response.ok(new Viewable("/templates/Permissions", pr)).build();
        } else {
            // accept all the always permissions
            List<ClientScope> clientScopes = getScopes(client, scopes);
            List<AcceptedScope> acceptedScopes = new ArrayList<>();
            for (ClientScope cs : clientScopes) {
                acceptedScopes.add(acceptScope(user, cs));
            }
            Token.Type type = getTokenType(responseType);
            // redirect with token since they've already asked for all the permissions
            Token t = generateToken(type, client, user, getExpires(client, type),
                redirectUri, acceptedScopes, null, null);
            return getRedirectResponse(redirectUri, state, t, rememberMe);
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
     * @param tkn         access or code token that was generated, depending on the type
     */
    private Response getRedirectResponse(String redirectUri, String state, Token tkn, boolean rememberMe) {
        UriBuilder toRedirect = UriBuilder.fromUri(redirectUri);

        String scope = tkn.getAcceptedScopes().stream()
            .map(AcceptedScope::getClientScope).map(ClientScope::getScope).map(Scope::getName)
            .collect(Collectors.joining(" "));
        String expiresIn = Long.toString(tkn.getExpires().getTime() - System.currentTimeMillis() / 1000L);

        if (tkn.getType().equals(Token.Type.ACCESS)) {
            MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
            params.putSingle("access_token", tkn.getToken());
            params.putSingle("token_type", TokenResponse.BEARER);
            if (state != null) {
                params.putSingle("state", state);
            }
            params.putSingle("expires_in", expiresIn);
            params.putSingle("scope", scope);
            toRedirect.fragment(mapToQueryString(params));
        }

        if (tkn.getType().equals(Token.Type.CODE)) {
            toRedirect.queryParam("code", tkn.getToken());
            if (state != null) {
                toRedirect.queryParam("state", state);
            }
        }

        return Response.status(Response.Status.FOUND)
            .location(toRedirect.build())
            .cookie(getNewCookie(tkn, rememberMe))
            .build();
    }

    @HeaderParam("X-Forwarded-Proto")
    private String forwardedProto;

    private NewCookie getNewCookie(Token tkn, Boolean rememberMe) {
        Date expires;
        LoginCookie loginCookie = getLoginCookie(tkn.getClient());
        if (loginCookie != null) {
            // we should re-use the same values
            expires = loginCookie.getExpires();
        } else {
            expires = new Date(System.currentTimeMillis() + ONE_MONTH);
            // we should issue a new cookie
            loginCookie = makeLoginCookie(tkn.getUser(), RandomStringUtil.randomAlphaNumeric(64), expires, rememberMe);
        }

        int maxAge = rememberMe ? (new Long(ONE_MONTH / 1000L)).intValue() : NewCookie.DEFAULT_MAX_AGE;
        Date expiry = rememberMe ? expires : null;

        boolean isHTTPS = "HTTPS".equalsIgnoreCase(forwardedProto);

        return new NewCookie(getCookieName(tkn.getClient()), loginCookie.getSecret(), "/", null, NewCookie.DEFAULT_VERSION,
            "login cookie", maxAge, expiry, isHTTPS, true);
    }

    private LoginCookie makeLoginCookie(User user, String secret, Date expires, boolean rememberMe) {
        LoginCookie loginCookie = new LoginCookie();
        loginCookie.setUser(user);
        loginCookie.setSecret(secret);
        loginCookie.setExpires(expires);
        loginCookie.setRememberMe(rememberMe);
        try {
            beginTransaction();
            em.persist(loginCookie);
            em.flush();
            commit();
        } catch (Exception e) {
            rollback();
            LOG.log(Level.SEVERE, "Failed to create a login cookie", e);
            loginCookie = null;
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
    private Token generateToken(Token.Type type, Token permissionToken, List<AcceptedScope> scopes) {
        return generateToken(type, permissionToken.getClient(), permissionToken.getUser(),
            getExpires(permissionToken.getClient(), type), permissionToken.getRedirectUri(), scopes, null, null);
    }

    /**
     * Get the permission token associated with a token string
     *
     * @param token  token string
     * @param client client the token was issued for
     * @return the token representing the client or null if it doesn't exist
     */
    private Token getPermissionToken(String token, Client client) {
        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> tk = tq.from(Token.class);
        List<Token> tks = em.createQuery(tq.select(tk).where(cb.and(
            cb.equal(tk.get("token"), token),
            cb.equal(tk.get("type"), Token.Type.PERMISSION),
            cb.equal(tk.get("client"), client)
        ))).getResultList();
        return tks.size() == 1 ? tks.get(0) : null;
    }

    /**
     * Generate a temporary token to represent correct credentials, giving the user 5 minutes to accept the permissions
     * before expiring
     *
     * @param user   the user to generate the token for
     * @param client the client to generate the token for
     * @return a temporary token for use on the permission form
     */
    private Token generatePermissionToken(User user, Client client, String redirectUri) {
        Token t = new Token();
        Date expires = new Date();
        expires.setTime(expires.getTime() + FIVE_MINUTES);
        t.setExpires(expires);
        t.setUser(user);
        t.setClient(client);
        t.setRandomToken(64);
        t.setType(Token.Type.PERMISSION);
        t.setRedirectUri(redirectUri);
        try {
            beginTransaction();
            em.persist(t);
            em.flush();
            commit();
        } catch (Exception e) {
            rollback();
            return null;
        }
        return t;
    }

    /**
     * Get the scopes that we need to show or ask the user about
     * ALWAYS permissions are not shown because the client always has those permissions when the user logs in
     * REQUIRED permissions are shown but the user does not have an option if they wish to log in to the client
     * ASK permissions are shown and the user has the option not to grant them to the service
     *
     * @param client client for which we're retrieving scopes
     * @param user   user for which we're retrieving scopes
     * @return list of scopes we should ask for
     */
    private List<ClientScope> getScopesToRequest(Client client, User user, List<String> scopes) {
        CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        Root<ClientScope> rcs = cq.from(ClientScope.class);

        List<Predicate> predicates = new ArrayList<>();

        // scopes for this client
        predicates.add(cb.equal(rcs.get("client"), client));

        // since a client will always have these scopes, we don't show them
        predicates.add(cb.notEqual(rcs.get("priority"), ClientScope.Priority.ALWAYS));

        // not already accepted by the user
        predicates.add(cb.not(rcs.in(acceptedScopes(user))));

        // scope names in this list
        if (scopes != null && scopes.size() > 0) {
            predicates.add(rcs.join("scope").get("name").in(scopes));
        }

        Predicate[] pArray = new Predicate[predicates.size()];
        predicates.toArray(pArray);

        return em.createQuery(cq.select(rcs).where(pArray)).getResultList();
    }

    /**
     * Generates a subquery that returns all the scopes a user has accepted
     *
     * @param user for which the acceptedscopes are returned
     * @return a subquery of all the clientscopes that have already been accepted by a user, across clients
     */
    private Subquery<ClientScope> acceptedScopes(User user) {
        Subquery<ClientScope> sq = cb.createQuery().subquery(ClientScope.class);

        Root<AcceptedScope> ras = sq.from(AcceptedScope.class);
        sq.select(ras.get("clientScope")).where(cb.equal(ras.get("user"), user));

        return sq;
    }

    /**
     * Get the user associated with an e-mail and an application
     *
     * @param email         user e-mail address
     * @param applicationId the application for which we're searching the user base
     * @return the User record
     */
    private User getUser(String email, Long applicationId) {
        CriteriaQuery<User> uq = cb.createQuery(User.class);
        Root<User> u = uq.from(User.class);

        List<User> users = em.createQuery(
            uq.select(u).where(
                cb.and(
                    cb.equal(u.join("application").get("id"), applicationId),
                    cb.equal(u.get("email"), email)
                )
            )
        ).getResultList();

        if (users.size() != 1) {
            return null;
        }
        return users.get(0);
    }

}
