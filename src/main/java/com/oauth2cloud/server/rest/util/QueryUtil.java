package com.oauth2cloud.server.rest.util;

import com.moodysalem.jaxrs.lib.resources.util.QueryHelper;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.db.*;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class QueryUtil {
    private static final Logger LOG = Logger.getLogger(QueryUtil.class.getName());

    /**
     * Get the scopes that a user has given a client permission to use
     *
     * @param client client to which scopes have been given
     * @param user   user that has given permission for these scopes
     * @return a list of accepted scopes
     */
    public static Set<AcceptedScope> getAcceptedScopes(final EntityManager em, final Client client, final User user) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        return new HashSet<>(
                QueryHelper.query(
                        em, AcceptedScope.class,
                        (ras) ->
                                cb.and(
                                        cb.equal(ras.join(AcceptedScope_.clientScope).get(ClientScope_.client), client),
                                        cb.equal(ras.get(AcceptedScope_.user), user)
                                )
                )
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
        if (isBlank(email)) {
            return null;
        }
        final CriteriaBuilder cb = em.getCriteriaBuilder();

        final List<User> users = QueryHelper.query(em, User.class, (u) ->
                cb.and(
                        cb.equal(u.get(User_.application), application),
                        cb.equal(u.get(User_.email), email)
                )
        );

        return expectOne(users);
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

        return new HashSet<>(
                QueryHelper.query(
                        em, ClientScope.class,
                        clientScopeRoot -> cb.and(
                                cb.equal(clientScopeRoot.get(ClientScope_.client), client),
                                (scopes != null && !scopes.isEmpty()) ?
                                        clientScopeRoot.join(ClientScope_.scope).get(Scope_.name).in(scopes) :
                                        cb.and()
                        )
                )
        );
    }

    public static LoginCode findLoginCode(final EntityManager em, final String token) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        return expectOne(QueryHelper.query(em, LoginCode.class, lc -> cb.equal(lc.get(LoginCode_.token), token)));
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

        final List<Client> clients = QueryHelper.query(em, Client.class, (client) ->
                cb.equal(client.join(Client_.credentials).get(ClientCredentials_.id), clientId)
        );
        return expectOne(clients);
    }


    /**
     * Get a token given the token string and the client it's for
     *
     * @param token  the token string
     * @param client the client it was issued to
     * @return the token or null if it doesn't exist or has expired
     */
    public static Token findToken(
            final EntityManager em,
            final String token,
            final Client client
    ) {
        if (isBlank(token)) {
            return null;
        }

        final CriteriaBuilder cb = em.getCriteriaBuilder();

        final List<Token> tokens = QueryHelper.query(em, Token.class, tokenRoot ->
                cb.and(
                        cb.greaterThan(tokenRoot.get(Token_.expires), System.currentTimeMillis()),
                        cb.equal(tokenRoot.get(Token_.token), token),
                        client != null ? cb.equal(tokenRoot.get(Token_.client), client) : cb.and()
                )
        );

        return expectOne(tokens);
    }

    /**
     * Generates a subquery that returns all the scopes a user has accepted (must be approved by application)
     *
     * @param user for which the acceptedscopes are returned
     * @return a subquery of all the clientscopes that have already been accepted by a user, across clients
     */
    public static Subquery<ClientScope> acceptedScopes(final CriteriaBuilder cb, final User user) {
        final Subquery<ClientScope> subquery = cb.createQuery().subquery(ClientScope.class);

        final Root<AcceptedScope> root = subquery.from(AcceptedScope.class);
        return subquery.select(root.get(AcceptedScope_.clientScope))
                .where(
                        cb.equal(root.get(AcceptedScope_.user), user)
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
            return TXHelper.withinTransaction(em, () -> em.merge(nu));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create user", e);
            return null;
        }
    }

    /**
     * Return the one element in a list
     *
     * @param list list of elements
     * @param <T>  type of element
     * @return one element
     */
    public static <T> T expectOne(List<T> list) {
        if (list == null || list.size() != 1) {
            return null;
        }
        return list.get(0);
    }

}
