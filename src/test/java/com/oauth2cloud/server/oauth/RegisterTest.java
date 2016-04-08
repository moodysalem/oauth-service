package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.rest.OAuth2Application;
import org.codemonkey.simplejavamail.email.Email;
import org.glassfish.jersey.client.ClientProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

public class RegisterTest extends OAuth2Test {

    // verify we can register for the application
    @Test
    public void testRegistration() throws URISyntaxException {
        Form up = new Form();
        up.param("firstName", "Moody");
        up.param("lastName", "Salem");
        up.param("registerEmail", "moody.salem+test@gmail.com");
        up.param("registerPassword", "moody");
        up.param("action", "register");

        Response register = target(OAuth2Application.OAUTH).path("authorize")
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("response_type", "token")
                .request()
                .post(Entity.form(up));

        assert register.getStatus() == 200;

        final Email e = getLastEmail();

        assert e.getRecipients().stream().anyMatch((r) -> r.getAddress().equals("moody.salem+test@gmail.com"));
        assert e.getFromRecipient().getAddress().equals("admin@oauth2cloud.com");

        Document doc = Jsoup.parse(e.getTextHTML());
        URI uri = new URI(doc.select("#verify-email-link").attr("href"));
        assert uri.getQuery().startsWith("code=");

        Response verify = client().target(uri).request().get();
        assert verify.getStatus() == 200;
        assert Jsoup.parse(verify.readEntity(String.class)).select(".alert.alert-success").size() > 0;

        assert getToken("moody.salem+test@gmail.com", "moody").getAccessToken() != null;
    }

}
