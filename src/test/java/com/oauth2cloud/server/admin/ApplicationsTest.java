package com.oauth2cloud.server.admin;

import com.oauth2cloud.server.OAuth2CloudTest;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.TokenResponse;
import com.oauth2cloud.server.rest.OAuth2Cloud;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;

public class ApplicationsTest extends OAuth2CloudTest {

    @Test
    public void testGet() {
        TokenResponse tr = getToken();

        assert target(OAuth2Cloud.API).path("applications")
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .get().getStatus() == 200;
    }

    @Test
    public void testCRUD() {
        TokenResponse tr = getToken();
        String faviconUrl = "https://google.com/test.png";
        Application app = new Application();
        app.setName("Test App");
        app.setSupportEmail("moody.salem@gmail.com");

        app = target(OAuth2Cloud.API).path("applications")
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .post(Entity.json(app), Application.class);

        assert app.getId() != 0;
        assert app.getName().equals("Test App");
        assert app.getFaviconUrl() == null;

        // test edit
        app.setFaviconUrl(faviconUrl);
        app = target(OAuth2Cloud.API).path("applications").path(Long.toString(app.getId()))
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .put(Entity.json(app), Application.class);

        assert app.getFaviconUrl().equals(faviconUrl);

        assert target(OAuth2Cloud.API).path("applications").path(Long.toString(app.getId()))
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .delete().getStatus() == 403;

    }

}
