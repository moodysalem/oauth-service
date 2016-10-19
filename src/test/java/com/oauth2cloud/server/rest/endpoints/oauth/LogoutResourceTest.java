package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import org.testng.annotations.Test;

public class LogoutResourceTest extends OAuth2Test {
    @Test
    public void testLogout() throws Exception {
        assert target("logout").request().get().getStatus() == 400;
        assert target("logout").queryParam("client_id", "abc").request().get().getStatus() == 400;
        assert target("logout").queryParam("client_id", CLIENT_ID).request().get().getStatus() == 204;
    }
}