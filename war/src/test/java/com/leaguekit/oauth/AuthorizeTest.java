package com.leaguekit.oauth;

import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.Assert.assertTrue;


public class AuthorizeTest extends OAuthTest {

    @Test
    public void testAuthorizeEndpoint() {
        Response r1 = target("authorize")
            .queryParam("client_id", 1).queryParam("redirect_uri", "http://localhost:8080").queryParam("response_type", "code")
            .request().get();

        Response r2 = target("authorize")
            .queryParam("client_id", 1).queryParam("redirect_uri", "http://localhost:8080").queryParam("response_type", "token")
            .request().get();

        assertTrue(r1.getStatus() == 200 && r2.getStatus() == 200);
    }

}
