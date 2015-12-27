package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2CloudTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.Assert.assertTrue;


public class AuthorizeTest extends OAuth2CloudTest {


    @Test
    public void testAuthorizeEndpoint() {
        Response r1 = target("oauth/authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "http://localhost:8080")
                .queryParam("response_type", "code")
                .request()
                .get();

        Response r2 = target("oauth/authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "http://localhost:8080")
                .queryParam("response_type", "token")
                .request()
                .get();

        assertTrue(r1.getStatus() == 400 && r2.getStatus() == 200);
    }

}
