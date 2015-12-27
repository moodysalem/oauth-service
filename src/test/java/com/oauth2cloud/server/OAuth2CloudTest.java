package com.oauth2cloud.server;

import com.oauth2cloud.server.hibernate.model.TokenResponse;
import com.oauth2cloud.server.rest.OAuth2Cloud;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class OAuth2CloudTest extends com.moodysalem.jaxrs.lib.test.BaseTest {
    public static final String AUTH_HEADER = "Authorization";
    public static final String CLIENT_ID = "6a63c1f1f10df85df6f918d68cb8c13e1e44856f7d861b05cbdd63bf7ea009f4";

    @Override
    public ResourceConfig getResourceConfig() {
        System.setProperty("JDBC_CONNECTION_STRING", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("JDBC_CONNECTION_USERNAME", "sa");
        System.setProperty("JDBC_CONNECTION_PASSWORD", "sa");
        System.setProperty("LIQUIBASE_CONTEXT", "test");

        System.setProperty("SMTP_HOST", "localhost");
        System.setProperty("SMTP_USERNAME", "fake");
        System.setProperty("SMTP_PASSWORD", "fake");

        System.setProperty("ENCRYPTION_SECRET", "xTUf4mP2SI6nfeLO");

        return new OAuth2Cloud();
    }


    /**
     * This method returns a TokenResponse corresponding to a log in to the admin application
     * from the admin user
     */
    public TokenResponse getToken() {

        Form up = new Form();
        up.param("email", "moody.salem@gmail.com")
            .param("password", "moody")
            .param("action", "login");

        Response loginScreen = target(OAuth2Cloud.OAUTH)
            .property(ClientProperties.FOLLOW_REDIRECTS, false)
            .path("authorize")
            .queryParam("client_id", CLIENT_ID)
            .queryParam("redirect_uri", "http://localhost:8080")
            .queryParam("response_type", "token")
            .request()
            .post(Entity.form(up));

        assert loginScreen.getStatus() == 302;

        String loc = loginScreen.getHeaderString("Location");

        try {
            URI u = new URI(loc);

            MultivaluedMap<String, String> values = new MultivaluedHashMap<>();
            TokenResponse tr = new TokenResponse();
            String frag = u.getFragment();
            String[] pcs = frag.split(Pattern.quote("&"));
            for (String pair : pcs) {
                String[] nv = pair.split(Pattern.quote("="));
                if (nv.length == 2) {
                    values.putSingle(
                        URLDecoder.decode(nv[0], "UTF-8"),
                        URLDecoder.decode(nv[1], "UTF-8")
                    );
                }
            }
            tr.setAccessToken(values.getFirst("access_token"));
            tr.setScope(values.getFirst("scope"));
            tr.setTokenType(values.getFirst("token_type"));
            tr.setExpiresIn(Long.parseLong(values.getFirst("expires_in")));
            return tr;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

    }
}
