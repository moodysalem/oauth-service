package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.hibernate.model.TokenResponse;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

public class TokenInfoTest extends OAuth2Test {

    @Test
    public void testToken() {
        TokenResponse tr = getToken();

        TokenResponse ti = target("oauth/token")
            .path("info")
            .request()
            .post(
                Entity.form(
                    new Form().param("application_id", "1")
                        .param("token", tr.getAccessToken())
                ),
                TokenResponse.class
            );

        assert ti.getAccessToken().equals(tr.getAccessToken());
        assert ti.getApplicationId().equals(1L);
        assert ti.getClientId().equals(CLIENT_ID);
        assert ti.getTokenType().equalsIgnoreCase("bearer");
        assert ti.getUserDetails() != null;
        assert ti.getUserDetails().getEmail().equals("moody.salem@gmail.com");
    }

}
