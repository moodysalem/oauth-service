package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2CloudTest;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

public class TokenInfoTest extends OAuth2CloudTest {

    @Test
    public void testToken() {
        Response ti = target("oauth/token")
                .path("info")
                .request()
                .post(
                        Entity.form(
                                new Form().param("application_id", "1")
                                        .param("token", getToken().getAccessToken())
                        )
                );

        assert ti.getStatus() == 200;
    }

}
