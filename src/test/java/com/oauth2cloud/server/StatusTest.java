package com.oauth2cloud.server;

import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class StatusTest extends OAuth2CloudTest {

    @Test
    public void testStatus() {
        Response r = target("status").request().get();
        assert r.getStatus() == 200;
    }
}
