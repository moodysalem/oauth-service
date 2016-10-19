package com.oauth2cloud.server;

import com.oauth2cloud.server.model.api.StatusResponse;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class StatusTest extends OAuth2Test {
    @Test
    public void testStatus() {
        final Response r = target("status").request().get();
        assert r.getStatus() == 200;
        final StatusResponse response = r.readEntity(StatusResponse.class);

        assert response.getDatabaseVersion() > 0 &&
                response.getVersion() != null &&
                !"${project.version}".equals(response.getVersion());
    }
}
