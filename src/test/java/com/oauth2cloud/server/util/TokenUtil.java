package com.oauth2cloud.server.util;

import com.oauth2cloud.server.SendsMail;
import com.oauth2cloud.server.model.api.TokenResponse;
import org.codemonkey.simplejavamail.email.Email;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;

public class TokenUtil {
    /**
     * This method returns a TokenResponse corresponding to a log in to the admin application
     * from the administrative user
     */
    public synchronized static TokenResponse getToken(final Client client,
                                         final WebTarget base,
                                         final SendsMail sendsMail,
                                         final String email,
                                         final String clientId,
                                         final String redirectUri) {
        final Form up = new Form();
        up.param("email", email).param("action", "email");

        final Response loginScreen = base.path("authorize")
                .property(FOLLOW_REDIRECTS, false)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "token")
                .request()
                .post(Entity.form(up));

        assert loginScreen.getStatus() == 200;

        // extract the login code link from the e-mail
        final Email lastEmail = sendsMail.lastSentEmail();
        final Document emailContent = Jsoup.parse(lastEmail.getTextHTML());
        final String loginLink = emailContent.select("#login-link").attr("href");

        final Response login = client.target(loginLink).property(FOLLOW_REDIRECTS, false).request().get();
        final String redirect = login.getHeaderString("Location");

        try {
            final URI u = new URI(redirect);

            final MultivaluedMap<String, String> values = new MultivaluedHashMap<>();
            final TokenResponse tokenResponse = new TokenResponse();
            final String frag = u.getFragment();
            final String[] pcs = frag.split(Pattern.quote("&"));
            for (final String pair : pcs) {
                final String[] nv = pair.split(Pattern.quote("="));
                if (nv.length == 2) {
                    values.putSingle(
                            URLDecoder.decode(nv[0], "UTF-8"),
                            URLDecoder.decode(nv[1], "UTF-8")
                    );
                }
            }
            tokenResponse.setAccessToken(values.getFirst("access_token"));
            tokenResponse.setScope(values.getFirst("scope"));
            tokenResponse.setExpiresIn(Long.parseLong(values.getFirst("expires_in")));
            return tokenResponse;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TokenResponse tokenInfo(final WebTarget base, final String token, final String clientId) {
        return tokenInfo(base, token, null, clientId);
    }

    public static TokenResponse tokenInfo(final WebTarget base, final String token, final UUID applicationId) {
        return tokenInfo(base, token, applicationId, null);
    }

    public static TokenResponse tokenInfo(final WebTarget base, final String token, final UUID applicationId, final String clientId) {
        final Form f = new Form();
        if (token != null) {
            f.param("token", token);
        }

        if (applicationId != null) {
            f.param("application_id", applicationId.toString());
        }

        if (clientId != null) {
            f.param("client_id", clientId);
        }

        return base.path("token").path("info")
                .request()
                .post(
                        Entity.form(f),
                        TokenResponse.class
                );
    }
}
