package com.oauth2cloud.server.hibernate.util;

import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.db.*;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class QueryUtil {
    private static final Logger LOG = Logger.getLogger(QueryUtil.class.getName());
    private static final long FIVE_MINUTES = 1000 * 60 * 5;

    public static ClientCallLog logCall(final EntityManager em, final Client client, final ContainerRequestContext request) {
        final CallLog cl = logCall(em, client, null, request);
        if (cl instanceof ClientCallLog) {
            return (ClientCallLog) cl;
        }
        return null;
    }

    public static ApplicationCallLog logCall(final EntityManager em, final Application application, final ContainerRequestContext request) {
        final CallLog cl = logCall(em, null, application, request);
        if (cl instanceof ApplicationCallLog) {
            return (ApplicationCallLog) cl;
        }
        return null;
    }

    /**
     * Log an API_PATH call
     *
     * @param client      client making the call
     * @param application application making the call
     * @return CallLog object that is created
     */
    private static CallLog logCall(final EntityManager em, final Client client, final Application application, final ContainerRequestContext request) {
        if (client == null && application == null) {
            throw new NullPointerException();
        }

        final CallLog callLog;

        if (client != null) {
            final ClientCallLog clientCallLog = new ClientCallLog();
            clientCallLog.setClient(client);
            callLog = clientCallLog;
        } else {
            final ApplicationCallLog applicationCallLog = new ApplicationCallLog();
            applicationCallLog.setApplication(application);
            callLog = applicationCallLog;
        }

        final String forwardedIp = request.getHeaderString("X-Forwaded-For");
        if (forwardedIp != null) {
            callLog.setIp(forwardedIp);
        } else {
            callLog.setIp("unknown");
        }
        callLog.setPath(request.getUriInfo().getPath());
        callLog.setMethod(request.getMethod());

        try {
            return TXHelper.withinTransaction(em, () -> em.merge(callLog));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to log a call", e);
            return null;
        }
    }

    /**
     * Get the scopes that a user has given a client permission to use
     *
     * @param client client to which scopes have been given
     * @param user   user that has given permission for these scopes
     * @return a list of accepted scopes
     */
    public static Set<AcceptedScope> getAcceptedScopes(final EntityManager em, final Client client, final User user) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<AcceptedScope> as = cb.createQuery(AcceptedScope.class);
        final Root<AcceptedScope> ras = as.from(AcceptedScope.class);

        return new HashSet<>(
                em.createQuery(
                        as.select(ras).where(
                                cb.equal(ras.join(AcceptedScope_.clientScope).get(ClientScope_.client), client),
                                cb.equal(ras.get(AcceptedScope_.user), user)
                        )
                ).getResultList()
        );
    }

    /**
     * Get the user associated with an e-mail and an application
     *
     * @param email       user e-mail address
     * @param application the application for which we're searching the user base
     * @return the User record
     */
    public static User getUser(final EntityManager em, final String email, final Application application) {
        if (StringUtils.isBlank(email)) {
            return null;
        }

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<User> uq = cb.createQuery(User.class);
        final Root<User> u = uq.from(User.class);

        final List<User> users = em.createQuery(
                uq.select(u).where(
                        cb.and(
                                cb.equal(u.get(User_.application), application),
                                cb.equal(u.get(User_.email), email)
                        )
                )
        ).getResultList();

        if (users.size() != 1) {
            return null;
        }
        return users.get(0);
    }


    /**
     * Look up a login cookie by the cookie value and the client (joined to application)
     *
     * @param secret secret of the cookie
     * @param client requesting client
     * @return LoginCookie for the secret and client
     */
    public static LoginCookie getLoginCookie(EntityManager em, String secret, Client client) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LoginCookie> lc = cb.createQuery(LoginCookie.class);
        final Root<LoginCookie> loginCookieRoot = lc.from(LoginCookie.class);
        lc.select(loginCookieRoot).where(
                cb.equal(loginCookieRoot.get(LoginCookie_.secret), secret),
                cb.greaterThan(loginCookieRoot.get(LoginCookie_.expires), System.currentTimeMillis()),
                cb.equal(loginCookieRoot.join(LoginCookie_.user).get(User_.application), client.getApplication())
        );
        final List<LoginCookie> loginCookies = em.createQuery(lc).getResultList();
        return (loginCookies.size() == 1) ? loginCookies.get(0) : null;
    }

    /**
     * Get a list of client scopes limited to scopes with names in the scopes list
     *
     * @param client client to get the scopes for
     * @param scopes filter to scopes with these names (null if you want all scopes)
     * @return list of scopes filtered to scopes with the names passed
     */
    public static Set<ClientScope> getScopes(final EntityManager em, final Client client, final Set<String> scopes) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        final Root<ClientScope> clientScopeRoot = cq.from(ClientScope.class);

        Predicate p = cb.equal(clientScopeRoot.get(ClientScope_.client), client);

        if (scopes != null && !scopes.isEmpty()) {
            p = cb.and(p, clientScopeRoot.join(ClientScope_.scope).get(Scope_.name).in(scopes));
        }
        return new HashSet<>(
                em.createQuery(cq.select(clientScopeRoot).where(p)).getResultList()
        );
    }

    /**
     * Accept a scope for a user
     *
     * @param user        accepting the scope
     * @param clientScope that is being accepted
     * @return the AcceptedScope that is created/found for the user/clientscope
     */
    public static AcceptedScope acceptScope(EntityManager em, User user, ClientScope clientScope) {
        final AcceptedScope existing = findAcceptedScope(em, user, clientScope);
        if (existing != null) {
            return existing;
        }
        final AcceptedScope newAs = new AcceptedScope();
        newAs.setUser(user);
        newAs.setClientScope(clientScope);

        try {
            return TXHelper.withinTransaction(em, () -> em.merge(newAs));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to accept scope", e);
            return null;
        }
    }

    /**
     * Find the accepted scope or return null if it's not yet accepted
     *
     * @param user        that has accepted the scope
     * @param clientScope to look for
     * @return AcceptedScope for the user/clientScope
     */
    public static AcceptedScope findAcceptedScope(EntityManager em, User user, ClientScope clientScope) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<AcceptedScope> acceptedScopeCriteriaQuery = cb.createQuery(AcceptedScope.class);
        final Root<AcceptedScope> acceptedScopeRoot = acceptedScopeCriteriaQuery.from(AcceptedScope.class);

        final List<AcceptedScope> acceptedScopes = em.createQuery(
                acceptedScopeCriteriaQuery.select(acceptedScopeRoot)
                        .where(

                                cb.equal(acceptedScopeRoot.get(AcceptedScope_.user), user),
                                cb.equal(acceptedScopeRoot.get(AcceptedScope_.clientScope), clientScope)
                        )
        ).getResultList();

        if (acceptedScopes.size() == 1) {
            return acceptedScopes.get(0);
        }
        return null;
    }


    /**
     * Get the client with a specific client ID
     *
     * @param clientId a client identifier
     * @return the Client corresponding to a client identifier
     */
    public static Client getClient(final EntityManager em, final String clientId) {
        if (clientId == null) {
            return null;
        }

        final CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<Client> clientCriteriaQuery = cb.createQuery(Client.class);
        final Root<Client> clientRoot = clientCriteriaQuery.from(Client.class);
        clientCriteriaQuery.select(clientRoot).where(
                cb.equal(clientRoot.join(Client_.credentials).get(ClientCredentials_.id), clientId)
        );

        List<Client> clients = em.createQuery(clientCriteriaQuery).getResultList();
        return (clients.size() != 1) ? null : clients.get(0);
    }


    /**
     * Get a token given the token string and the client it's for
     *
     * @param token  the token string
     * @param client the client it was issued to
     * @return the token or null if it doesn't exist or has expired
     */
    public static Token findToken(final EntityManager em, final String token, final Client client, final Collection<TokenType> types) {
        if (token == null) {
            return null;
        }
        final CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        final Root<Token> tokenRoot = tq.from(Token.class);

        Predicate p = cb.and(
                cb.equal(tokenRoot.get(Token_.token), token),
                types != null && !types.isEmpty() ? tokenRoot.get(Token_.type).in(types) : cb.and(),
                cb.greaterThan(tokenRoot.get(Token_.expires), System.currentTimeMillis()),
                cb.isNull(tokenRoot.get(Token_.user))
        );

        if (client != null) {
            p = cb.and(p, cb.equal(tokenRoot.get(Token_.client), client));
        }

        final List<Token> tkns = em.createQuery(tq.select(tokenRoot).where(p)).getResultList();
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
    public static Token generateToken(
            final EntityManager em,
            final TokenType type,
            final Client client,
            final User user,
            final Date expires,
            final String redirectUri,
            final Set<AcceptedScope> scopes,
            final Token refreshToken,
            final Set<ClientScope> clientScopes
    ) {
        final Token toReturn = new Token();
        toReturn.setClient(client);
        toReturn.setExpires(expires);
        toReturn.setUser(user);
        toReturn.setType(type);
        toReturn.setRedirectUri(redirectUri);
        toReturn.setRandomToken(64);
        toReturn.setAcceptedScopes(scopes);
        toReturn.setRefreshToken(refreshToken);
        toReturn.setClientScopes(clientScopes);

        try {
            return TXHelper.withinTransaction(em, () -> em.merge(toReturn));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create a token", e);
            return null;
        }
    }

    /**
     * Generate a temporary token to represent correct credentials, giving the user 5 minutes to accept the permissions
     * before expiring
     *
     * @param user   the user to generate the token for
     * @param client the client to generate the token for
     */
    public static Token generatePermissionToken(EntityManager em, User user, Client client, String redirectUri) {
        final Token t = new Token();
        final Date expires = new Date();
        expires.setTime(expires.getTime() + FIVE_MINUTES);
        t.setExpires(expires);
        t.setUser(user);
        t.setClient(client);
        t.setRandomToken(64);
        t.setType(TokenType.PERMISSION);
        t.setRedirectUri(redirectUri);
        try {
            return TXHelper.withinTransaction(em, () -> em.merge(t));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to generate permission token", e);
            return null;
        }
    }


    /**
     * Get the permission token associated with a token string
     *
     * @param token  token string
     * @param client client the token was issued for
     * @return the token representing the client or null if it doesn't exist
     */
    public static Token getPermissionToken(EntityManager em, String token, Client client) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        final Root<Token> tk = tq.from(Token.class);
        final List<Token> tks = em.createQuery(tq.select(tk).where(cb.and(
                cb.equal(tk.get(Token_.token), token),
                cb.equal(tk.get(Token_.type), TokenType.PERMISSION),
                cb.equal(tk.get(Token_.client), client)
        ))).getResultList();
        return tks.size() == 1 ? tks.get(0) : null;
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
    public static Set<ClientScope> getScopesToRequest(final EntityManager em, final Client client, final User user, Set<String> scopes) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        final Root<ClientScope> rcs = cq.from(ClientScope.class);

        final List<Predicate> predicates = new LinkedList<>();

        // since a client will always have these scopes, we don't show them
        predicates.add(cb.notEqual(rcs.get(ClientScope_.priority), ClientScope.Priority.ALWAYS));

        // not already accepted by the user
        predicates.add(cb.not(rcs.in(acceptedScopes(em, user))));

        // scope names in this list
        if (scopes != null && !scopes.isEmpty()) {
            predicates.add(rcs.join(ClientScope_.scope).get(Scope_.name).in(scopes));
        }

        return new HashSet<>(
                em.createQuery(cq.select(rcs).where(predicates.stream().toArray(Predicate[]::new))).getResultList()
        );
    }

    /**
     * Generates a subquery that returns all the scopes a user has accepted (must be approved by application)
     *
     * @param user for which the acceptedscopes are returned
     * @return a subquery of all the clientscopes that have already been accepted by a user, across clients
     */
    public static Subquery<ClientScope> acceptedScopes(EntityManager em, User user) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final Subquery<ClientScope> sq = cb.createQuery().subquery(ClientScope.class);

        final Root<AcceptedScope> ras = sq.from(AcceptedScope.class);
        return sq.select(ras.get(AcceptedScope_.clientScope))
                .where(
                        cb.equal(ras.get(AcceptedScope_.user), user)
                );
    }

    /**
     * Find or create a user for a particular application
     *
     * @param app   application to create user for
     * @param email email address of user
     * @return user object created/found
     */
    public static User findOrCreateUser(final EntityManager em, final Application app, final String email) {
        final User existing = QueryUtil.getUser(em, email, app);

        if (existing != null) {
            return existing;
        }

        final User nu = new User();
        nu.setApplication(app);
        nu.setEmail(email);
        try {
            return TXHelper.withinTransaction(em, () -> em.merge(existing));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create user", e);
            return null;
        }
    }

    /**
     * Get all the access tokens for a user for a particular client that expire after today
     *
     * @param em
     * @param client
     * @param user
     * @return
     */
    public static Token getNewestUserAccessToken(final EntityManager em, final Client client, final User user) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Token> query = cb.createQuery(Token.class);
        final Root<Token> tokenRoot = query.from(Token.class);

        query.select(tokenRoot)
                .where(
                        cb.equal(tokenRoot.get(Token_.client), client),
                        cb.equal(tokenRoot.get(Token_.user), user),
                        cb.equal(tokenRoot.get(Token_.type), TokenType.ACCESS),
                        cb.greaterThan(tokenRoot.get(Token_.expires), System.currentTimeMillis())
                )
                .orderBy(cb.desc(tokenRoot.get(Token_.expires)));

        final List<Token> results = em.createQuery(query).setMaxResults(1).getResultList();

        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

}
