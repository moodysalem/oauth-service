package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.rest.OAuth2Application;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

public class ResetPasswordFormTest extends OAuth2Test {

    @Test
    public void testFormGet() {
        Response r = target(OAuth2Application.OAUTH).path("reset")
                .queryParam("applicationId", APPLICATION_ID)
                .request().get();

        assert r.getStatus() == 200;

        Document doc = Jsoup.parse(r.readEntity(String.class));

        assert "OAuth2Cloud".equals(doc.select("h1.page-header").first().text());
        Element fr = doc.select("form#form-reset").first();
        assert fr != null;
        assert "POST".equalsIgnoreCase(fr.attr("method"));
        assert "Reset Password".equals(fr.select("h3").first().text());
        assert fr.select("#back").size() == 0 && fr.select("#submitReset").size() == 1;
    }

    @Test
    public void testFormGetWithReferrer() {
        Response r = target(OAuth2Application.OAUTH).path("reset")
                .queryParam("applicationId", APPLICATION_ID)
                .queryParam("referrer", "http://localhost:8080")
                .request().get();

        assert r.getStatus() == 200;

        Document doc = Jsoup.parse(r.readEntity(String.class));

        assert "OAuth2Cloud".equals(doc.select("h1.page-header").first().text());
        Element fr = doc.select("form#form-reset").first();
        assert fr != null;
        assert "POST".equalsIgnoreCase(fr.attr("method"));
        assert "Reset Password".equals(fr.select("h3").first().text());
        assert fr.select("#back").size() == 1 && fr.select("#submitReset").size() == 1;
    }

    @Test
    public void testPost() {
        Form f = new Form();
        f.param("email", "moody.salem@gmail.com");
        Response r = target(OAuth2Application.OAUTH).path("reset")
                .queryParam("applicationId", APPLICATION_ID)
                .queryParam("referrer", "http://localhost:8080")
                .request()
                .post(Entity.form(f));
        assert r.getStatus() == 200;

        Document doc = Jsoup.parse(r.readEntity(String.class));

        assert "OAuth2Cloud".equals(doc.select("h1.page-header").first().text());
        Element fr = doc.select("form#form-reset").first();
        assert fr != null;
        assert "POST".equalsIgnoreCase(fr.attr("method"));
        assert "Reset Password".equals(fr.select("h3").first().text());
        assert fr.select("#back").size() == 1 && fr.select("#submitReset").size() == 1;

        // assert a success message
        assert doc.select(".alert.alert-success").size() == 1;

        assert getLastEmail() != null;
        assert getLastEmail().getRecipients().stream()
                .allMatch((rec) -> rec.getAddress().equals("moody.salem@gmail.com"));
    }


}
