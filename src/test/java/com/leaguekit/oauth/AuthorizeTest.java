package com.leaguekit.oauth;

import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.Assert.assertEquals;


public class AuthorizeTest extends OAuthTest {

    @Test
    public void testAuthorizeEndpoint() {
        Response r = target("authorize").request().get();
        assertEquals(r.getStatus(), Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

}
