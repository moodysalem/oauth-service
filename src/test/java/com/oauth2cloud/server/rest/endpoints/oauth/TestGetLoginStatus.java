package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import org.testng.annotations.Test;

import javax.ws.rs.client.WebTarget;

public class TestGetLoginStatus extends OAuth2Test {


    /**
     * Verify that the login status endpoint returns appropriate error codes
     * when not coming from an iframe
     */
    @Test
    public void verifyMustHaveReferer() {
        final WebTarget loginStatus = target("login-status");

        assert loginStatus.request().get().getStatus() == 400;

        assert loginStatus.queryParam("client_id", CLIENT_ID).request().get().getStatus() == 400;

        assert loginStatus.queryParam("client_id", CLIENT_ID).request()
                .header("Referer", "https://moodysalem.com").get().getStatus() == 400;

        assert loginStatus.queryParam("client_id", CLIENT_ID + "a").request()
                .header("Referer", "https://oauth2cloud.com").get().getStatus() == 400;


        assert loginStatus.queryParam("client_id", CLIENT_ID).request()
                .header("Referer", "https://oauth2cloud.com").get().getStatus() == 200;
    }

}
