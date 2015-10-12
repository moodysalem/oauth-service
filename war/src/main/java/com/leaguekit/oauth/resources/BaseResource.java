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
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseResource {
    private static final String AUTH_SESSION = "AUTH_SESSION";
    private static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;

    @CookieParam(AUTH_SESSION)
    private Cookie authSession;

    protected Logger LOG = Logger.getLogger(BaseResource.class.getName());

    @Context
    HttpServletRequest req;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    private EntityTransaction etx;

    private Session session;

    @PostConstruct
    public void init() {
        cb = cb == null ? em.getCriteriaBuilder() : cb;
        initSession();
    }

    private void initSession() {
        if (authSession != null && authSession.getValue() != null) {
            session = getSession(authSession.getValue());
        }
        if (session == null) {
            session = makeSession();
        }
    }

    private Session getSession(String secret) {
        CriteriaQuery<Session> cq = cb.createQuery(Session.class);
        Root<Session> sr = cq.from(Session.class);
        List<Session> ls = em.createQuery(cq.select(sr).where(
            cb.equal(sr.get("secret"), secret),
            cb.greaterThan(sr.<Date>get("expires"), new Date())
        )).getResultList();
        if (ls.size() == 1) {
            return ls.get(0);
        }
        return null;
    }

    private Session makeSession() {
        Session s = new Session();
        s.setSecret(RandomStringUtil.randomAlphaNumeric(64));
        s.setExpires(new Date(System.currentTimeMillis() + ONE_MONTH));
        try {
            beginTransaction();
            em.persist(s);
            commit();
        } catch (Exception e) {
            rollback();
            LOG.log(Level.SEVERE, "Failed to generate a session.", e);
        }
        return s;
    }

    private NewCookie getSessionCookie() {
        try {
            boolean isHTTPS = "HTTPS".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
            URI reqUri = (new URI(req.getRequestURI()));
            return new NewCookie(AUTH_SESSION, session.getSecret(), "/", reqUri.getHost(), null,
                -1, isHTTPS, true);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    protected Response addCookie(Response.ResponseBuilder rb) {
        if (session != null) {
            if (authSession == null || !session.getSecret().equals(authSession.getValue())) {
                return rb.cookie(getSessionCookie()).build();
            }
        }
        return rb.build();
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
                                  List<AcceptedScope> scopes, Token refreshToken) {
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
