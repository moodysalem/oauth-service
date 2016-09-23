package com.oauth2cloud.server.hibernate.util;

import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.model.db.AcceptedScope_;
import com.oauth2cloud.server.model.db.ClientScope_;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.*;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OldQueryHelper {
    private static final Logger LOG = Logger.getLogger(OldQueryHelper.class.getName());
    private static final long FIVE_MINUTES = 1000 * 60 * 5;


    public static CallLog logCall(final EntityManager em, final Client client, final ContainerRequestContext request) {
        return logCall(em, client, null, request);
    }

    public static CallLog logCall(final EntityManager em, final Application application, final ContainerRequestContext request) {
        return logCall(em, null, application, request);
    }

    /**
     * Log an API call
     *
     * @param client      client making the call
     * @param application application making the call
     * @return CallLog object that is created
     */
    private static CallLog logCall(final EntityManager em, final Client client, final Application application, final ContainerRequestContext request) {
        if (client == null && application == null) {
            throw new NullPointerException();
        }
        final CallLog cl = new CallLog();
        if (client != null) {
            cl.setClient(client);
            cl.setApplication(client.getApplication());
        } else {
            cl.setApplication(application);
        }
        final String forwardedIp = request.getHeaderString("X-Forwaded-For");
        if (forwardedIp != null) {
            cl.setIp(forwardedIp);
        } else {
            cl.setIp("unknown");
        }
        cl.setPath(request.getUriInfo().getPath());
        cl.setMethod(request.getMethod());
        final EntityTransaction etx = em.getTransaction();
        try {
            etx.begin();
            em.persist(cl);
            etx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to log a call", e);
            etx.rollback();
            return null;
        }
        return cl;
    }

    /**
     * Get the scopes that a user has given a client permission to use
     *
     * @param client client to which scopes have been given
     * @param user   user that has given permission for these scopes
     * @return a list of accepted scopes
     */
    public static List<AcceptedScope> getAcceptedScopes(EntityManager em, Client client, User user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AcceptedScope> as = cb.createQuery(AcceptedScope.class);
        Root<AcceptedScope> ras = as.from(AcceptedScope.class);
        return em.createQuery(as.select(ras).where(
                cb.equal(ras.join(AcceptedScope_.clientScope).get(ClientScope_.client), client),
                cb.equal(ras.get(AcceptedScope_.user), user)
        )).getResultList();
    }


    /**
     * Make a UserCode
     *
     * @param user     user for which the code is created
     * @param referrer the referrer to which the user should be redirected after using the code
     * @param type     the type of code
     * @param expires  when it expires
     * @return UserCode created
     */
    public static UserCode makeUserCode(EntityManager em, User user, String referrer, UserCode.Type type, Date expires) {
        UserCode pw = new UserCode();
        pw.setExpires(expires);
        pw.setUser(user);
        pw.setCode(RandomStringUtils.randomAlphanumeric(64));
        pw.setReferrer(referrer);
        pw.setType(type);
        pw.setExpires(expires);
        EntityTransaction etx = em.getTransaction();
        try {
            etx.begin();
            em.persist(pw);
            etx.commit();
        } catch (Exception e) {
            etx.rollback();
            pw = null;
            LOG.log(Level.SEVERE, "Failed to create user code", e);
        }
        return pw;
    }


    /**
     * Get a UserCode based on the user code string
     */
    public static UserCode getUserCode(EntityManager em, String code, UserCode.Type type, boolean includeUsed) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        if (code == null) {
            return null;
        }
        CriteriaQuery<UserCode> pw = cb.createQuery(UserCode.class);
        Root<UserCode> userCodeRoot = pw.from(UserCode.class);
        Predicate queryPredicate = cb.and(
                cb.equal(userCodeRoot.get(UserCode_.code), code),
                cb.greaterThan(userCodeRoot.get(UserCode_.expires), new Date()),
                cb.equal(userCodeRoot.get(UserCode_.type), type)
        );

        if (!includeUsed) {
            queryPredicate = cb.and(
                    queryPredicate,
                    cb.equal(userCodeRoot.get(UserCode_.used), false)
            );
        }

        pw.select(userCodeRoot).where(queryPredicate);
        List<UserCode> lp = em.createQuery(pw).getResultList();
        return lp.size() == 1 ? lp.get(0) : null;
    }


    /**
     * Get the user associated with an e-mail and an application
     *
     * @param email       user e-mail address
     * @param application the application for which we're searching the user base
     * @return the User record
     */
    public static User getUser(EntityManager em, String email, Application application) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> uq = cb.createQuery(User.class);
        Root<User> u = uq.from(User.class);

        List<User> users = em.createQuery(
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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LoginCookie> lc = cb.createQuery(LoginCookie.class);
        Root<LoginCookie> loginCookieRoot = lc.from(LoginCookie.class);
        lc.select(loginCookieRoot).where(
                cb.equal(loginCookieRoot.get(LoginCookie_.secret), secret),
                cb.greaterThan(loginCookieRoot.get(LoginCookie_.expires), new Date()),
                cb.equal(loginCookieRoot.join(LoginCookie_.user).get(User_.application), client.getApplication()),
                cb.equal(loginCookieRoot.join(LoginCookie_.user).get(User_.active), true),
                cb.equal(loginCookieRoot.join(LoginCookie_.user).join(User_.application).get(Application_.active), true)
        );
        List<LoginCookie> loginCookies = em.createQuery(lc).getResultList();
        return (loginCookies.size() == 1) ? loginCookies.get(0) : null;
    }

    /**
     * Get a list of client scopes limited to scopes with names in the scopes list
     *
     * @param client client to get the scopes for
     * @param scopes filter to scopes with these names (null if you want all scopes)
     * @return list of scopes filtered to scopes with the names passed
     */
    public static List<ClientScope> getScopes(EntityManager em, Client client, List<String> scopes) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        Root<ClientScope> clientScopeRoot = cq.from(ClientScope.class);
        Predicate p = cb.and(cb.equal(clientScopeRoot.get(ClientScope_.client), client), cb.equal(clientScopeRoot.get(ClientScope_.approved), true));
        if (scopes != null && scopes.size() > 0) {
            p = cb.and(p, clientScopeRoot.join(ClientScope_.scope).get(Scope_.name).in(scopes));
        }
        return em.createQuery(cq.select(clientScopeRoot).where(p)).getResultList();
    }

    /**
     * Accept a scope for a user
     *
     * @param user        accepting the scope
     * @param clientScope that is being accepted
     * @return the AcceptedScope that is created/found for the user/clientscope
     */
    public static AcceptedScope acceptScope(EntityManager em, User user, ClientScope clientScope) {
        AcceptedScope as = findAcceptedScope(em, user, clientScope);
        if (as != null) {
            return as;
        }
        as = new AcceptedScope();
        as.setUser(user);
        as.setClientScope(clientScope);

        EntityTransaction etx = em.getTransaction();
        try {
            etx.begin();
            em.persist(as);
            em.flush();
            etx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to accept a scope", e);
            etx.rollback();
            return null;
        }
        return as;
    }


    /**
     * Find the accepted scope or return null if it's not yet accepted
     *
     * @param user        that has accepted the scope
     * @param clientScope to look for
     * @return AcceptedScope for the user/clientScope
     */
    public static AcceptedScope findAcceptedScope(EntityManager em, User user, ClientScope clientScope) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<AcceptedScope> acceptedScopeCriteriaQuery = cb.createQuery(AcceptedScope.class);
        Root<AcceptedScope> acceptedScopeRoot = acceptedScopeCriteriaQuery.from(AcceptedScope.class);

        List<AcceptedScope> acceptedScopes = em.createQuery(
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
    public static Client getClient(EntityManager em, String clientId) {
        if (clientId == null) {
            return null;
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Client> clientCriteriaQuery = cb.createQuery(Client.class);
        Root<Client> clientRoot = clientCriteriaQuery.from(Client.class);
        clientCriteriaQuery.select(clientRoot);
        clientCriteriaQuery.where(
                cb.equal(clientRoot.get(Client_.identifier), clientId),
                cb.equal(clientRoot.get(Client_.active), true),
                cb.equal(clientRoot.join(Client_.application).get(Application_.active), true)
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
    public static Token getToken(EntityManager em, String token, Client client, Token.Type... types) {
        if (token == null) {
            return null;
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> tokenRoot = tq.from(Token.class);

        Predicate p = cb.and(
                cb.equal(tokenRoot.get(Token_.token), token),
                tokenRoot.get(Token_.type).in(types),
                cb.greaterThan(tokenRoot.get(Token_.expires), new Date()),
                cb.or(
                        cb.equal(tokenRoot.join(Token_.user).get(User_.active), true),
                        cb.isNull(tokenRoot.get(Token_.user))
                ),
                cb.equal(tokenRoot.join(Token_.client).get(Client_.active), true),
                cb.equal(tokenRoot.join(Token_.client).join(Client_.application).get(Application_.active), true)
        );
        if (client != null) {
            p = cb.and(p, cb.equal(tokenRoot.get(Token_.client), client));
        }

        List<Token> tkns = em.createQuery(tq.select(tokenRoot).where(p)).getResultList();
        return tkns.size() == 1 ? tkns.get(0) : null;
    }

    /**
     * Create and persist a token
     *
     * @param type                the token's type
     * @param client              the client for which the token is being created
     * @param user                the to which the token is associated
     * @param expires             when the token becomes invalid
     * @param scopes              the scopes for which the token is valid
     * @param provider            the provider that was used to get this token
     * @param providerAccessToken the access token from the provider used to log in
     * @return a Token with the aforementioned properties
     */
    public static Token generateToken(EntityManager em, Token.Type type, Client client, User user, Date expires, String redirectUri,
                                      List<AcceptedScope> scopes, Token refreshToken, List<ClientScope> clientScopes,
                                      Provider provider, String providerAccessToken) {
        Token toReturn = new Token();
        toReturn.setClient(client);
        toReturn.setExpires(expires);
        toReturn.setUser(user);
        toReturn.setType(type);
        toReturn.setRedirectUri(redirectUri);
        toReturn.setRandomToken(64);
        toReturn.setAcceptedScopes(scopes);
        toReturn.setRefreshToken(refreshToken);
        toReturn.setClientScopes(clientScopes);
        toReturn.setProvider(provider);
        toReturn.setProviderAccessToken(providerAccessToken);
        EntityTransaction etx = em.getTransaction();
        try {
            etx.begin();
            em.persist(toReturn);
            em.flush();
            etx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create a token", e);
            etx.rollback();
            return null;
        }
        return toReturn;
    }

    /**
     * Generate a temporary token to represent correct credentials, giving the user 5 minutes to accept the permissions
     * before expiring
     *
     * @param user                the user to generate the token for
     * @param client              the client to generate the token for
     * @param provider
     * @param providerAccessToken @return a temporary token for use on the permission form
     */
    public static Token generatePermissionToken(EntityManager em, User user, Client client, String redirectUri, Provider provider, String providerAccessToken) {
        Token t = new Token();
        Date expires = new Date();
        expires.setTime(expires.getTime() + FIVE_MINUTES);
        t.setExpires(expires);
        t.setUser(user);
        t.setClient(client);
        t.setRandomToken(64);
        t.setType(Token.Type.PERMISSION);
        t.setRedirectUri(redirectUri);
        t.setProvider(provider);
        t.setProviderAccessToken(providerAccessToken);
        EntityTransaction etx = em.getTransaction();
        try {
            etx.begin();
            em.persist(t);
            em.flush();
            etx.commit();
        } catch (Exception e) {
            etx.rollback();
            return null;
        }
        return t;
    }


    /**
     * Get the permission token associated with a token string
     *
     * @param token  token string
     * @param client client the token was issued for
     * @return the token representing the client or null if it doesn't exist
     */
    public static Token getPermissionToken(EntityManager em, String token, Client client) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> tk = tq.from(Token.class);
        List<Token> tks = em.createQuery(tq.select(tk).where(cb.and(
                cb.equal(tk.get(Token_.token), token),
                cb.equal(tk.get(Token_.type), Token.Type.PERMISSION),
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
    public static List<ClientScope> getScopesToRequest(EntityManager em, Client client, User user, List<String> scopes) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        Root<ClientScope> rcs = cq.from(ClientScope.class);

        List<Predicate> predicates = new ArrayList<>();

        // scopes for this client
        predicates.add(cb.equal(rcs.get(ClientScope_.client), client));

        // only approved scopes should be asked for
        predicates.add(cb.equal(rcs.get(ClientScope_.approved), true));

        // since a client will always have these scopes, we don't show them
        predicates.add(cb.notEqual(rcs.get(ClientScope_.priority), ClientScope.Priority.ALWAYS));

        // not already accepted by the user
        predicates.add(cb.not(rcs.in(acceptedScopes(em, user))));

        // scope names in this list
        if (scopes != null && scopes.size() > 0) {
            predicates.add(rcs.join(ClientScope_.scope).get(Scope_.name).in(scopes));
        }

        Predicate[] pArray = new Predicate[predicates.size()];
        predicates.toArray(pArray);

        List<ClientScope> results = em.createQuery(cq.select(rcs).where(pArray)).getResultList();

        results.sort(null);

        return results;
    }

    /**
     * Generates a subquery that returns all the scopes a user has accepted (must be approved by application)
     *
     * @param user for which the acceptedscopes are returned
     * @return a subquery of all the clientscopes that have already been accepted by a user, across clients
     */
    public static Subquery<ClientScope> acceptedScopes(EntityManager em, User user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Subquery<ClientScope> sq = cb.createQuery().subquery(ClientScope.class);

        Root<AcceptedScope> ras = sq.from(AcceptedScope.class);
        sq.select(ras.get(AcceptedScope_.clientScope)).where(
                cb.equal(ras.get(AcceptedScope_.user), user),
                cb.equal(ras.join(AcceptedScope_.clientScope).get(ClientScope_.approved), true),
                cb.equal(ras.join(AcceptedScope_.clientScope).join(ClientScope_.scope).get(Scope_.active), true),
                cb.equal(ras.join(AcceptedScope_.clientScope).join(ClientScope_.client).get(Client_.active), true)
        );

        return sq;
    }


}
