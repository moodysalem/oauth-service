package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2CloudTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.Assert.assertTrue;

public class StatusTest extends OAuth2CloudTest {

    @Test
    public void testStatus() {
        Response r = target("status").request().get();
        assertTrue(r.getStatus() == 200);
    }
}
