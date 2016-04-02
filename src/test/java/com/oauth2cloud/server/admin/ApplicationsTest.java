package com.oauth2cloud.server.admin;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.hibernate.model.TokenResponse;
import com.oauth2cloud.server.rest.OAuth2Application;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;

public class ApplicationsTest extends OAuth2Test {

    @Test
    public void testGet() {
        TokenResponse tr = getToken();

        assert target(OAuth2Application.API).path("applications")
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .get().getStatus() == 200;
    }

    @Test
    public void testCRUD() {
        TokenResponse tr = getToken();
        String faviconUrl = "https://google.com/test.png";
        com.oauth2cloud.server.hibernate.model.Application app = new com.oauth2cloud.server.hibernate.model.Application();
        app.setName("Test App");
        app.setSupportEmail("moody.salem@gmail.com");

        app = target(OAuth2Application.API).path("applications")
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .post(Entity.json(app), com.oauth2cloud.server.hibernate.model.Application.class);

        assert app.getId() != null;
        assert app.getName().equals("Test App");
        assert app.getFaviconUrl() == null;

        // test edit
        app.setFaviconUrl(faviconUrl);
        app = target(OAuth2Application.API).path("applications").path(app.getId().toString())
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .put(Entity.json(app), com.oauth2cloud.server.hibernate.model.Application.class);

        assert app.getFaviconUrl().equals(faviconUrl);

        assert target(OAuth2Application.API).path("applications").path(app.getId().toString())
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .delete().getStatus() == 403;

    }

}
