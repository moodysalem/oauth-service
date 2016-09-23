package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.endpoints.oauth.OAuthResource;
import org.testng.annotations.Test;

import javax.ws.rs.client.WebTarget;

public class TestGetLoginStatus extends OAuth2Test {

    /**
     * Verify that the login status endpoint returns appropriate error codes
     * when not coming from an iframe
     */
    @Test
    public void testLoginStatus() {
        WebTarget wt = target(OAuth2Application.OAUTH_PATH).path("loginstatus");
        assert wt.request().get().getStatus() == 400;

        WebTarget wtCid = wt.queryParam("client_id", CLIENT_ID);
        assert wtCid.request().get().getStatus() == 400;

        assert wtCid.request()
            .header("Referer", "https://oauth2cloud.com").get().getStatus() == 200;

        assert wtCid.request()
            .header("Referer", "https://oauth2cloud.com")
            .header("Cookie", OAuthResource.COOKIE_NAME_PREFIX + APPLICATION_ID + "=abc").get().getStatus() == 200;
    }


}
