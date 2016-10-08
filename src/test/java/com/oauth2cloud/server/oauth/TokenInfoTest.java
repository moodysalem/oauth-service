package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.TokenResponse;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

public class TokenInfoTest extends OAuth2Test {

    @Test
    public void testToken() {
        final TokenResponse tr = getToken();

        final TokenResponse ti = target("token")
            .path("info")
            .request()
            .post(
                Entity.form(
                    new Form().param("application_id", APPLICATION_ID.toString())
                        .param("token", tr.getAccessToken())
                ),
                TokenResponse.class
            );

        assert ti.getAccessToken().equals(tr.getAccessToken());
        assert ti.getApplicationId().equals(APPLICATION_ID);
        assert ti.getClientId().equals(CLIENT_ID);
        assert ti.getTokenType().equalsIgnoreCase("bearer");
        assert ti.getUser() != null;
        assert ti.getUser().getEmail().equals("moody.salem@gmail.com");
    }

}
