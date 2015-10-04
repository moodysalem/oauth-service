package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.AcceptedScope;
import com.leaguekit.oauth.model.Client;
import com.leaguekit.oauth.model.ClientScope;
import com.leaguekit.oauth.model.User;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Path("authorize")
@Produces(MediaType.TEXT_HTML)
public class AuthorizeResource extends BaseResource {

    public static final String TOKEN = "token";
    public static final String TOKEN_SESSION_ATTRIBUTE = "token";
    public static final String CODE = "code";
    public static final String INVALID_E_MAIL_OR_PASSWORD = "Invalid e-mail or password.";

    // this returns an error view if there are any issues with the response type, client id, or redirect URI
    // these errors are primarily for the developer interfacing with the oauth login
    private Viewable validateParameters(String responseType, String clientId, String redirectUri) {
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

    @GET
    public Viewable auth(
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri,
        @QueryParam("state") String state
    ) {
        Viewable error = validateParameters(responseType, clientId, redirectUri);
        if (error != null) {
            return error;
        }


        AuthorizeResponse ar = new AuthorizeResponse();
        ar.setClient(getClient(clientId));

        return new Viewable("/templates/Login", ar);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Viewable login(
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri,
        @QueryParam("state") String state,
        @QueryParam("scope") List<String> scopes,
        @FormParam("email") String email,
        @FormParam("password") String password
    ) {
        // validate the client id stuff again
        Viewable error = validateParameters(responseType, clientId, redirectUri);
        if (error != null) {
            return error;
        }

        AuthorizeResponse ar = new AuthorizeResponse();
        Client c = getClient(clientId);
        ar.setClient(c);

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
                        List<ClientScope> toAsk = getScopes(c, u, scopes);
                        if (toAsk.size() > 0) {
                            // we need to generate a temporary token for them to get to the next step with
                        } else {
                            // redirect with token
                        }
                    } else {
                        ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
                    }
                }
            }
        } else {
            ar.setLoginError(INVALID_E_MAIL_OR_PASSWORD);
        }

        return new Viewable("/templates/Login", ar);
    }

    /**
     * Get the scopes that we need to show or ask the user for
     *
     * @param client client for which we're retrieving scopes
     * @param user   user for which we're retrieving scopes
     * @return list of scopes we should ask for
     */
    private List<ClientScope> getScopes(Client client, User user, List<String> scopes) {
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
