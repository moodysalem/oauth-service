package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2CloudTest;
import com.oauth2cloud.server.rest.OAuth2Cloud;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

public class ResetPasswordFormTest extends OAuth2CloudTest {

    @Test
    public void testFormGet() {
        Response r = target(OAuth2Cloud.OAUTH).path("reset")
            .queryParam("applicationId", 1)
            .request().get();

        assert r.getStatus() == 200;

        Document doc = Jsoup.parse(r.readEntity(String.class));

        assert "OAuth2 Cloud".equals(doc.select("h1.page-header").first().text());
        Element fr = doc.select("form#form-reset").first();
        assert fr != null;
        assert "POST".equalsIgnoreCase(fr.attr("method"));
        assert "Reset Password".equals(fr.select("h3").first().text());
        assert fr.select("#back").size() == 0 && fr.select("#submitReset").size() == 1;
    }

    @Test
    public void testFormGetWithReferrer() {
        Response r = target(OAuth2Cloud.OAUTH).path("reset")
            .queryParam("applicationId", 1)
            .queryParam("referrer", "http://localhost:8080")
            .request().get();

        assert r.getStatus() == 200;

        Document doc = Jsoup.parse(r.readEntity(String.class));

        assert "OAuth2 Cloud".equals(doc.select("h1.page-header").first().text());
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
        Response r = target(OAuth2Cloud.OAUTH).path("reset")
            .queryParam("applicationId", 1)
            .queryParam("referrer", "http://localhost:8080")
            .request()
            .post(Entity.form(f));
        assert r.getStatus() == 200;

        Document doc = Jsoup.parse(r.readEntity(String.class));

        assert "OAuth2 Cloud".equals(doc.select("h1.page-header").first().text());
        Element fr = doc.select("form#form-reset").first();
        assert fr != null;
        assert "POST".equalsIgnoreCase(fr.attr("method"));
        assert "Reset Password".equals(fr.select("h3").first().text());
        assert fr.select("#back").size() == 1 && fr.select("#submitReset").size() == 1;

        // assert a success message
        assert doc.select(".alert.alert-success").size() == 1;
    }



}
