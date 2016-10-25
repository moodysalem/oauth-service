package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.ClientCredentials;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

import java.util.UUID;

public class ApplicationsTest extends OAuth2Test {
    @Test
    public void testCrudApplications() {
        final TokenResponse response = getToken();
        final Crud<Application> crud = new Crud<>(Application.class, target("applications"), response.getAccessToken());

        final Application app = new Application();
        assert crud.saveResponse(app).getStatus() == 422;
        app.setName(UUID.randomUUID().toString());
        assert crud.saveResponse(app).getStatus() == 422;

        app.setSupportEmail("moody.salem@gmail.com");
        assert crud.saveResponse(app).getStatus() == 200;
        assert crud.saveResponse(app).getStatus() == 422;
        app.setName(null);
        assert crud.saveResponse(app).getStatus() == 422;
        app.setName(UUID.randomUUID().toString());

        assert crud.delete(crud.save(app)).getStatus() == 204;

        Application saved = crud.save(app);
        saved.setGoogleCredentials(new ClientCredentials(null, null));
        assert crud.saveResponse(saved).getStatus() == 422;

        saved.setGoogleCredentials(new ClientCredentials("abc", "123"));
        assert crud.saveResponse(saved).getStatus() == 200;
        saved = crud.get(saved.getId());
        assert saved.getGoogleCredentials() != null;
        assert saved.getGoogleCredentials().getId().equals("abc");
        assert saved.getGoogleCredentials().getSecret().equals("123");

        saved.setGoogleCredentials(new ClientCredentials("abc", null));
        assert crud.saveResponse(saved).getStatus() == 422;

        saved.setGoogleCredentials(new ClientCredentials(null, "abc"));
        assert crud.saveResponse(saved).getStatus() == 422;
    }
}
