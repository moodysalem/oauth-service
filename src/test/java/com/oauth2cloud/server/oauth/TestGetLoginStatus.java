package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2CloudTest;
import com.oauth2cloud.server.rest.OAuth2Cloud;
import com.oauth2cloud.server.rest.resources.BaseResource;
import org.testng.annotations.Test;

import javax.ws.rs.client.WebTarget;

public class TestGetLoginStatus extends OAuth2CloudTest {

    @Test
    public void testLoginStatus() {
        WebTarget wt = target(OAuth2Cloud.OAUTH).path("loginstatus");
        assert wt.request().get().getStatus() == 400;

        WebTarget wtCid = wt.queryParam("client_id",
            "6a63c1f1f10df85df6f918d68cb8c13e1e44856f7d861b05cbdd63bf7ea009f4");
        assert wtCid.request().get().getStatus() == 400;

        assert wtCid.request()
            .header("Referer", "http://localhost:8080").get().getStatus() == 200;

        assert wtCid.request()
            .header("Referer", "http://localhost:8080")
            .header("Cookie", BaseResource.COOKIE_NAME_PREFIX + "1=abc").get().getStatus() == 200;
    }


}
