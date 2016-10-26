package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.rest.util.CookieUtil;
import org.codemonkey.simplejavamail.email.Email;
import org.jsoup.Jsoup;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.net.URI;

public class LogoutResourceTest extends OAuth2Test {
    @Test
    public void testValidatesRequest() throws Exception {
        assert target("logout").request().get().getStatus() == 400;
        assert target("logout").queryParam("client_id", "abc").request().get().getStatus() == 400;
        assert target("logout").queryParam("client_id", CLIENT_ID).request().get().getStatus() == 204;
    }

    private Cookie getCookie() {
        // send an e-mail
        assert target("authorize")
                .queryParam("response_type", "token")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("state", "abc-123")
                .request()
                .post(
                        Entity.form(
                                new Form()
                                        .param("email", "moody.salem@gmail.com")
                                        .param("remember_me", "on")
                                        .param("action", "email")
                        )
                ).getStatus() == 200;

        final Email email = lastSentEmail();
        assert email != null;

        final String loginUrl = Jsoup.parse(email.getTextHTML()).select("#login-link").attr("href");
        assert loginUrl != null;

        final Response loginResponse = client().target(loginUrl)
                .request().get();

        assert loginResponse.getStatus() == 302;
        final URI loc = loginResponse.getLocation();
        assert loc != null;
        assert loc.getScheme().equalsIgnoreCase("https");
        assert loc.getHost().equalsIgnoreCase("oauth2cloud.com");
        assert loc.getFragment().contains("state=abc-123");

        final Cookie cookie = loginResponse.getCookies().get(CookieUtil.COOKIE_NAME_PREFIX + APPLICATION_ID);
        final String cookieHeader = loginResponse.getHeaderString("Set-Cookie");
        assert cookieHeader.contains(";Path=/;");
        assert cookieHeader.contains(";Max-Age=2592000;");
        assert cookieHeader.contains(";Secure;");
        assert cookieHeader.contains(";HttpOnly;");

        assert cookie != null;
        return cookie;
    }

    @Test
    public void testRememberMe() {
        final Cookie cookie = getCookie();

        final Response remembered = target("authorize")
                .queryParam("response_type", "token")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("state", "123-abc")
                .request()
                .cookie(cookie)
                .get();

        assert remembered.getStatus() == 302;
        final URI redirect = remembered.getLocation();
        assert redirect.getScheme().equalsIgnoreCase("https");
        assert redirect.getHost().equalsIgnoreCase("oauth2cloud.com");
        assert redirect.getFragment().contains("state=123-abc");
    }

    @Test
    public void testExpiresCookie() {
        final Cookie cookie = getCookie();

        // log out with the cookie
        assert target("logout").queryParam("client_id", CLIENT_ID).request().cookie(cookie).get().getStatus() == 204;

        assert target("authorize")
                .queryParam("response_type", "token")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("state", "123-abc")
                .request()
                .cookie(cookie)
                .get().getStatus() == 200;
    }

    @Test
    public void testLogoutQueryParam() {
        final Cookie cookie = getCookie();

        assert target("authorize")
                .queryParam("response_type", "token")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("state", "123-abc")
                .queryParam("logout", true)
                .request()
                .cookie(cookie)
                .get().getStatus() == 200;

        assert target("authorize")
                .queryParam("response_type", "token")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("state", "123-abc")
                .request()
                .cookie(cookie)
                .get().getStatus() == 200;
    }
}