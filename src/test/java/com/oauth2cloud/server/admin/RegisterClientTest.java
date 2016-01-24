package com.oauth2cloud.server.admin;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.rest.OAuth2Application;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class RegisterClientTest extends OAuth2Test {
    @Test
    public void testGetInfo() {
        Response r = target(OAuth2Application.API).path("publicapplications").path("1").path("info")
            .request().get();
        assert r.getStatus() == 200;
    }
}
