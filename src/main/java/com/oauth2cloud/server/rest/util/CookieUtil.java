package com.oauth2cloud.server.rest.util;

import com.moodysalem.jaxrs.lib.resources.util.QueryHelper;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.db.*;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public abstract class CookieUtil {
    public static final String COOKIE_NAME_PREFIX = "_AID_";
    private static final Logger LOG = Logger.getLogger(CookieUtil.class.getName());

    /**
     * Get the cookie given by the name
     *
     * @param name name of the cookie to look up
     * @return a Cookie
     */
    private static Cookie getCookie(final ContainerRequestContext req, final String name) {
        return req.getCookies().get(name);
    }

    /**
     * Get the Cookie object for a specific client
     *
     * @param client the client for which a cookie should be found
     * @return the Cookie corresponding to the client
     */
    private static Cookie getCookie(ContainerRequestContext req, final Client client) {
        if (client == null) {
            return null;
        }
        return getCookie(req, getCookieName(client));
    }

    /**
     * Get the name of a cookie that a client should be using
     */
    private static String getCookieName(final Client client) {
        return String.format("%s%s", COOKIE_NAME_PREFIX, client.getApplication().getId());
    }

    /**
     * Get the LoginCookie for a specific client
     *
     * @param client to find the login cookie for
     * @return the login cookie
     */
    public static LoginCookie getLoginCookie(
            final EntityManager em,
            final ContainerRequestContext req,
            final Client client
    ) {
        final Cookie cookie = getCookie(req, client);
        if (cookie == null) {
            return null;
        }
        // the cookie value is the login cookie secret
        final String secret = cookie.getValue();
        return getLoginCookie(em, secret, client);
    }

    /**
     * Look up a login cookie by the cookie value and the client (joined to application)
     *
     * @param secret secret of the cookie
     * @param client requesting client
     * @return LoginCookie for the secret and client
     */
    public static LoginCookie getLoginCookie(
            final EntityManager em,
            final String secret,
            final Client client
    ) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();

        final List<LoginCookie> loginCookies = QueryHelper.query(em, LoginCookie.class, (root) ->
                cb.and(
                        cb.equal(root.get(LoginCookie_.secret), secret),
                        cb.greaterThan(root.get(LoginCookie_.expires), System.currentTimeMillis()),
                        cb.equal(root.join(LoginCookie_.user).get(User_.application), client.getApplication())
                )
        );

        return QueryUtil.expectOne(loginCookies);
    }


    private static final Pattern IP_ADDRESS = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

    /**
     * Get a new login cookie
     *
     * @param em            used to persist cookie
     * @param user          user that logged in
     * @param client        client that has logged in
     * @param requestDomain request domain
     * @return
     */
    public static NewCookie getNewCookie(
            final EntityManager em,
            final User user,
            final Client client,
            final String requestDomain
    ) {
        final LoginCookie loginCookie = generateLoginCookie(em, user);
        if (loginCookie == null) {
            return null;
        }

        final String cookieDomain;
        if (requestDomain != null) {
            // don't set a cookie domain if the request came straight to the IP address
            if (IP_ADDRESS.matcher(requestDomain).matches()) {
                cookieDomain = null;
            } else {
                // the domain should be the last two pieces of the domain name
                final List<String> domainPieces = Arrays.asList(requestDomain.split("\\."));
                cookieDomain = domainPieces.subList(
                        Math.max(0, domainPieces.size() - 2),
                        domainPieces.size()
                ).stream().collect(Collectors.joining("."));
            }
        } else {
            cookieDomain = null;
        }

        return new NewCookie(
                getCookieName(client),
                loginCookie.getSecret(),
                "/",
                cookieDomain,
                NewCookie.DEFAULT_VERSION,
                null,
                60 * 60 * 24 * 30,
                loginCookie.getExpires(),
                true,
                true
        );
    }

    /**
     * Generate a login cookie
     *
     * @param em   used to persist cookie
     * @param user for whom the cookie represents a succesful login
     * @return a login cookie
     */
    private static LoginCookie generateLoginCookie(final EntityManager em, final User user) {
        final LoginCookie loginCookie = new LoginCookie();
        loginCookie.setUser(user);
        loginCookie.expiresInOneMonth();
        loginCookie.setSecret(randomAlphanumeric(96));

        try {
            return TXHelper.withinTransaction(em, () -> em.merge(loginCookie));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create a login cookie", e);
            return null;
        }
    }
}
