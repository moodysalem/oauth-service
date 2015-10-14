package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;
import com.leaguekit.oauth.model.*;
import com.leaguekit.util.RandomStringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseResource {
    private static final String AUTH_SESSION = "AUTH_SESSION";
    public static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;
    public static final long THREE_SECONDS = 3000L;

    protected Logger LOG = Logger.getLogger(BaseResource.class.getName());

    @Context
    HttpServletRequest req;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    private EntityTransaction etx;

    @PostConstruct
    public void init() {
        cb = cb == null ? em.getCriteriaBuilder() : cb;
    }

    /**
     * Get a token given the token string and the client it's for
     *
     * @param token  the token string
     * @param client the client it was issued to
     * @return the token or null if it doesn't exist or has expired
     */
    protected Token getToken(String token, Client client, Token.Type... types) {
        if (token == null) {
            return null;
        }
        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> t = tq.from(Token.class);
        tq.select(t).where(
            cb.and(
                cb.equal(t.get("token"), token),
                t.get("type").in(types),
                cb.greaterThan(t.<Date>get("expires"), new Date()),
                cb.equal(t.get("client"), client)
            )
        );

        List<Token> tkns = em.createQuery(tq).getResultList();
        return tkns.size() == 1 ? tkns.get(0) : null;
    }

    /**
     * Create and persist a token
     *
     * @param type    the token's type
     * @param client  the client for which the token is being created
     * @param user    the to which the token is associated
     * @param expires when the token becomes invalid
     * @param scopes  the scopes for which the token is valid
     * @return a Token with the aforementioned properties
     */
    protected Token generateToken(Token.Type type, Client client, User user, Date expires, String redirectUri,
                                  List<AcceptedScope> scopes, Token refreshToken, List<ClientScope> clientScopes) {
        Token toReturn = new Token();
        toReturn.setClient(client);
        toReturn.setExpires(expires);
        toReturn.setUser(user);
        toReturn.setType(type);
        toReturn.setRedirectUri(redirectUri);
        toReturn.setRandomToken(64);
        toReturn.setAcceptedScopes(scopes);
        toReturn.setRefreshToken(refreshToken);
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

    /**
     * Helper function to calculate when a token should expire based on the client's TTL
     *
     * @param client for which the token is being generated
     * @return when the token should expire
     */
    protected Date getExpires(Client client, boolean refresh) {
        if (refresh && client.getRefreshTokenTtl() == null) {
            throw new IllegalArgumentException();
        }
        return new Date(System.currentTimeMillis() + (refresh ? client.getRefreshTokenTtl() : client.getTokenTtl()) * 1000);
    }

    private HashMap<String, Client> clientCache = new HashMap<>();

    /**
     * Get the client with a specific client ID
     *
     * @param clientId a client identifier
     * @return the Client corresponding to a client identifier
     */
    protected Client getClient(String clientId) {
        if (clientId == null) {
            return null;
        }
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

    /**
     * Helper function that converts a map to its query string representation. This is used when setting the fragment
     * in the response URI of a token grant flow
     *
     * @param map of parameters to generate the query string for
     * @return a query string style representation of the map
     */
    protected String mapToQueryString(MultivaluedMap<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            for (String value : map.get(key)) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                try {
                    sb.append(URLEncoder.encode(key, "UTF-8")).append('=').append(URLEncoder.encode(value, "UTF-8"));
                } catch (Exception ignored) {
                    LOG.log(Level.SEVERE, "Failed to encode map", ignored);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Find the accepted scope or return null if it's not yet accepted
     *
     * @param user        that has accepted the scope
     * @param clientScope to look for
     * @return AcceptedScope for the user/clientScope
     */
    protected AcceptedScope findAcceptedScope(User user, ClientScope clientScope) {
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

    /**
     * Accept a scope for a user
     *
     * @param user        accepting the scope
     * @param clientScope that is being accepted
     * @return the AcceptedScope that is created/found for the user/clientscope
     */
    protected AcceptedScope acceptScope(User user, ClientScope clientScope) {
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

    /**
     * Get a list of client scopes limited to scopes with names in the scopes list
     *
     * @param client client to get the scopes for
     * @param scopes filter to scopes with these names (null if you want all scopes)
     * @return list of scopes filtered to scopes with the names passed
     */
    protected List<ClientScope> getScopes(Client client, List<String> scopes) {
        CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        Root<ClientScope> rcs = cq.from(ClientScope.class);
        Predicate p = cb.equal(rcs.get("client"), client);
        if (scopes != null && scopes.size() > 0) {
            p = cb.and(p, rcs.join("scope").get("name").in(scopes));
        }
        return em.createQuery(cq.select(rcs).where(p)).getResultList();
    }

    protected void beginTransaction() {
        if (etx != null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Nested Transactions Opened");
        }
        LOG.info("Beginning transaction");
        etx = em.getTransaction();
        etx.begin();
    }

    protected void commit() {
        if (etx == null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Transaction committed while not open");
        }
        LOG.info("Committing transaction");
        etx.commit();
        etx = null;
    }

    protected void rollback() {
        if (etx != null) {
            LOG.info("Rolling back transaction");
            etx.rollback();
            etx = null;
        }
    }

}
