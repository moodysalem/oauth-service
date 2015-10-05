package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.*;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Path("authorize")
@Produces(MediaType.TEXT_HTML)
public class AuthorizeResource extends BaseResource {

    public static final String TOKEN = "token";
    public static final String CODE = "code";
    public static final String INVALID_E_MAIL_OR_PASSWORD = "Invalid e-mail or password.";
    public static final int FIVE_MINUTES = (1000 * 60 * 5);
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "An internal server error occurred. Please try again later.";
    public static final String SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN = "Something went wrong. Please try again.";
    public static final String YOUR_LOGIN_ATTEMPT_HAS_EXPIRED_PLEASE_TRY_AGAIN = "Your login attempt has expired. Please try again.";
    public static final String BEARER = "bearer";


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

    // this returns an error view if there are any issues with the response type, client id, or redirect URI
    // these errors are primarily for the developer interfacing with the oauth login
    private Viewable validateParameters(String responseType, String clientId, String redirectUri, List<String> scopes) {
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

        return null;
    }

    public static class AuthorizeResponse {
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

    public static class PermissionsResponse {
        private Token token;
        private List<ClientScope> clientScopes;

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
    }

    @GET
    public Viewable auth() {
        Viewable error = validateParameters(responseType, clientId, redirectUri, scopes);
        if (error != null) {
            return error;
        }


        AuthorizeResponse ar = new AuthorizeResponse();
        ar.setClient(getClient(clientId));

        return new Viewable("/templates/Login", ar);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Viewable login(MultivaluedMap<String, String> formParams) {
        String email = formParams.getFirst("email");
        String password = formParams.getFirst("password");
        String loginToken = formParams.getFirst("login_token");
        // validate the client id stuff again
        Viewable error = validateParameters(responseType, clientId, redirectUri, scopes);
        if (error != null) {
            return error;
        }

        AuthorizeResponse ar = new AuthorizeResponse();
        Client c = getClient(clientId);
        ar.setClient(c);

        // they just completed the second step of the login
        if (loginToken != null) {
            Token t = getPermissionToken(loginToken, c);
            if (t == null) {
                ar.setLoginError(SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
            } else {
                if (t.getExpires().before(new Date())) {
                    ar.setLoginError(YOUR_LOGIN_ATTEMPT_HAS_EXPIRED_PLEASE_TRY_AGAIN);
                } else {
                    // first get all the client scopes we will try to approve or check if are approved
                    List<ClientScope> clientScopes = getScopes(c);
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
                    doRedirect(redirectUri, state, token);
                }
            }
        } else {
            // validate the username and password
            if (email != null && password != null) {
                User u = getUser(email, c.getApplication().getId());
                if (u == null) {
                    ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
                } else {
                    if (u.getPassword() == null) {
                        ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
                    } else {
                        if (BCrypt.checkpw(password, u.getPassword())) {
                            // successfully authenticated the user
                            List<ClientScope> toAsk = getScopesToRequest(c, u, scopes);
                            if (toAsk.size() > 0) {
                                // we need to generate a temporary token for them to get to the next step with
                                Token t = generatePermissionToken(u, c);
                                if (t == null) {
                                    ar.setLoginError(INTERNAL_SERVER_ERROR_MESSAGE);
                                } else {
                                    PermissionsResponse pr = new PermissionsResponse();
                                    pr.setClientScopes(toAsk);
                                    pr.setToken(t);
                                    return new Viewable("/templates/Permissions", pr);
                                }
                            } else {
                                // redirect with token since they've already asked for all the permissions
                                Token t = generateToken(getTokenType(responseType), c, u, getExpires(c), findAcceptedScopes(u, c));
                                doRedirect(redirectUri, state, t);
                            }
                        } else {
                            ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
                        }
                    }
                }
            } else {
                ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
            }
        }

        return new Viewable("/templates/Login", ar);
    }

    private Token.Type getTokenType(String responseType) {
        return CODE.equalsIgnoreCase(responseType) ? Token.Type.CODE : Token.Type.ACCESS;
    }

    private void doRedirect(String redirectUri, String state, Token tkn) {
        UriBuilder toRedirect = UriBuilder.fromUri(redirectUri);

        String scope = tkn.getAcceptedScopes().stream()
            .map(AcceptedScope::getClientScope).map(ClientScope::getScope).map(Scope::getName)
            .collect(Collectors.joining(" "));
        String expiresIn = Long.toString((tkn.getExpires().getTime() - (new Date()).getTime()) / 1000);

        if (tkn.getType().equals(Token.Type.ACCESS)) {
            Map<String, String> params = new HashMap<>();
            params.put("access_token", tkn.getToken());
            params.put("token_type", BEARER);
            if (state != null) {
                params.put("state", state);
            }
            params.put("expires_in", expiresIn);
            params.put("scope", scope);
            toRedirect.fragment(mapToQueryString(params));
        }

        if (tkn.getType().equals(Token.Type.CODE)) {
            toRedirect.queryParam("code", tkn.getToken());
            if (state != null) {
                toRedirect.queryParam("state", state);
            }
        }
        throw new RedirectionException(302, toRedirect.build());
    }

    private String mapToQueryString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (HashMap.Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            try {
                sb.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
            } catch (Exception ignored) {
                LOG.log(Level.SEVERE, "Failed to encode map", ignored);
            }
        }
        return sb.toString();
    }

    private Date getExpires(Client c) {
        return new Date((new Date()).getTime() + c.getTokenTtl() * 1000);
    }

    private Token generateToken(Token.Type type, Token permissionToken, List<AcceptedScope> scopes) {
        return generateToken(type, permissionToken.getClient(), permissionToken.getUser(),
            getExpires(permissionToken.getClient()), scopes);
    }

    private Token generateToken(Token.Type type, Client client, User user, Date expires, List<AcceptedScope> scopes) {
        Token toReturn = new Token();
        toReturn.setClient(client);
        toReturn.setExpires(expires);
        toReturn.setUser(user);
        toReturn.setType(type);
        toReturn.setRandomToken(64);
        toReturn.setAcceptedScopes(scopes);
        try {
            beginTransaction();
            em.persist(toReturn);
            em.flush();
            commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create a token", e);
            rollback();
            return null;
        }
        return toReturn;
    }

    private List<AcceptedScope> findAcceptedScopes(User user, Client client) {
        CriteriaQuery<AcceptedScope> cas = cb.createQuery(AcceptedScope.class);
        Root<AcceptedScope> ras = cas.from(AcceptedScope.class);
        List<AcceptedScope> las = em.createQuery(cas.select(ras).where(cb.and(
            cb.equal(ras.get("user"), user),
            cb.equal(ras.join("clientScope").get("client"), client)
        ))).getResultList();
        return las;
    }

    private AcceptedScope findAcceptedScope(User user, ClientScope clientScope) {
        CriteriaQuery<AcceptedScope> cas = cb.createQuery(AcceptedScope.class);
        Root<AcceptedScope> ras = cas.from(AcceptedScope.class);
        List<AcceptedScope> las = em.createQuery(cas.select(ras).where(cb.and(
            cb.equal(ras.get("user"), user),
            cb.equal(ras.get("clientScope"), clientScope)
        ))).getResultList();
        if (las.size() == 1) {
            return las.get(0);
        }
        return null;
    }

    private AcceptedScope acceptScope(User user, ClientScope clientScope) {
        AcceptedScope as = findAcceptedScope(user, clientScope);
        if (as != null) {
            return as;
        }
        as = new AcceptedScope();
        as.setUser(user);
        as.setClientScope(clientScope);

        try {
            beginTransaction();
            em.persist(as);
            em.flush();
            commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to accept a scope", e);
            rollback();
            return null;
        }
        return as;
    }

    private List<ClientScope> getScopes(Client c) {
        CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        Root<ClientScope> rcs = cq.from(ClientScope.class);
        return em.createQuery(cq.select(rcs).where(cb.equal(rcs.get("client"), c))).getResultList();
    }

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

    private Token generatePermissionToken(User user, Client client) {
        Token t = new Token();
        Date expires = new Date();
        expires.setTime(expires.getTime() + FIVE_MINUTES);
        t.setExpires(expires);
        t.setUser(user);
        t.setClient(client);
        t.setRandomToken(64);
        t.setType(Token.Type.PERMISSION);
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
     * Get the scopes that we need to show or ask the user for
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

    private Subquery<ClientScope> acceptedScopes(User user) {
        Subquery<ClientScope> sq = cb.createQuery().subquery(ClientScope.class);

        Root<AcceptedScope> ras = sq.from(AcceptedScope.class);
        sq.select(ras.get("clientScope")).where(cb.equal(ras.get("user"), user));

        return sq;
    }

    private Viewable error(String error) {
        return new Viewable("/templates/Error", error);
    }

    private HashMap<String, Client> clientCache = new HashMap<>();

    private Client getClient(String clientId) {
        if (clientCache.containsKey(clientId)) {
            return clientCache.get(clientId);
        }

        CriteriaQuery<Client> cq = cb.createQuery(Client.class);
        Root<Client> ct = cq.from(Client.class);
        cq.select(ct);
        cq.where(cb.equal(ct.get("identifier"), clientId));

        List<Client> cts = em.createQuery(cq).getResultList();
        Client c = (cts.size() != 1) ? null : cts.get(0);
        clientCache.put(clientId, c);
        return c;
    }

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
