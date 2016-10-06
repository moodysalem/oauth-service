package com.oauth2cloud.server.rest.util;

import com.oauth2cloud.server.hibernate.util.QueryUtil;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;

import javax.persistence.EntityManager;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;

public class CookieUtil {
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
        return QueryUtil.getLoginCookie(em, secret, client);
    }
}
