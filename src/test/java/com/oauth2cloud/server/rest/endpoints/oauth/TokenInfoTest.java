package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.util.TokenUtil;
import org.testng.annotations.Test;

public class TokenInfoTest extends OAuth2Test {

    @Test
    public void testToken() {
        final TokenResponse tr = getToken();

        TokenResponse ti = TokenUtil.tokenInfo(target(), tr.getAccessToken(), APPLICATION_ID);
        assert ti.getAccessToken().equals(tr.getAccessToken());
        assert ti.getApplicationId().equals(APPLICATION_ID);
        assert ti.getClientId().equals(CLIENT_ID);
        assert ti.getTokenType().equalsIgnoreCase("bearer");
        assert ti.getUser() != null;
        assert ti.getUser().getEmail().equals("moody.salem@gmail.com");

        ti = TokenUtil.tokenInfo(target(), tr.getAccessToken(), CLIENT_ID);
        assert ti.getAccessToken().equals(tr.getAccessToken());
        assert ti.getApplicationId().equals(APPLICATION_ID);
        assert ti.getClientId().equals(CLIENT_ID);
        assert ti.getTokenType().equalsIgnoreCase("bearer");
        assert ti.getUser() != null;
        assert ti.getUser().getEmail().equals("moody.salem@gmail.com");
    }

}
