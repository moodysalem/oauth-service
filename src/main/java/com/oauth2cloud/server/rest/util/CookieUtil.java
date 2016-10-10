package com.oauth2cloud.server.rest.util;

import com.moodysalem.jaxrs.lib.resources.util.QueryHelper;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;
import com.oauth2cloud.server.model.db.LoginCookie_;
import com.oauth2cloud.server.model.db.User_;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import java.util.List;

public abstract class CookieUtil {
    public static final String COOKIE_NAME_PREFIX = "_AID_";

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
    public static String getCookieName(final Client client) {
        return String.format("%s%s", COOKIE_NAME_PREFIX, client.getApplication().getId());
    }

    /**
     * Get the LoginCookie for a specific client
     *
     * @param client to find the login cookie for
     * @return the login cookie
     */
    public static LoginCookie getLoginCookie(final EntityManager em, final ContainerRequestContext req, final Client client) {
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
    public static LoginCookie getLoginCookie(final EntityManager em, final String secret, final Client client) {
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
}
