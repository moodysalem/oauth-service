package com.oauth2cloud.server.admin;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import java.util.UUID;

public class ApplicationsTest extends OAuth2Test {

    @Test
    public void testGet() {
        TokenResponse tr = getToken();

        assert target(OAuth2Application.API_PATH).path("applications")
                .request()
                .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
                .get().getStatus() == 200;
    }

    @Test
    public void testCreateAndEdit() {
        TokenResponse tr = getToken();
        String faviconUrl = "https://google.com/test.png";
        Application app = new Application();
        app.setName("Test App Creation");
        UUID id = UUID.randomUUID();
        app.setId(id);
        app.setSupportEmail("moody.salem@gmail.com");

        app = target(OAuth2Application.API_PATH).path("applications")
                .request()
                .header(AUTH_HEADER, TokenFilter.BEARER + tr.getAccessToken())
                .post(Entity.json(app), Application.class);

        assert id.equals(app.getId());
        assert app.getName().equals("Test App Creation");
        assert app.getFaviconUrl() == null;

        // test edit
        app.setFaviconUrl(faviconUrl);
        app.setName("Test App Editing");
        app = target(OAuth2Application.API_PATH).path("applications").path(app.getId().toString())
                .request()
                .header(AUTH_HEADER, TokenFilter.BEARER  + tr.getAccessToken())
                .put(Entity.json(app), Application.class);

        assert app.getName().equals("Test App Editing");
        assert app.getFaviconUrl().equals(faviconUrl);

        assert target(OAuth2Application.API_PATH).path("applications").path(app.getId().toString())
                .request()
                .header(AUTH_HEADER, TokenFilter.BEARER  + tr.getAccessToken())
                .delete().getStatus() == 403;

    }

}
