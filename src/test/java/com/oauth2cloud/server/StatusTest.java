package com.oauth2cloud.server;

import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class StatusTest extends OAuth2Test {
    @Test
    public void testStatus() {
        final Response r = target("status").request().get();
        assert r.getStatus() == 200;
        assert r.readEntity(String.class).equals("OK");
    }
}
