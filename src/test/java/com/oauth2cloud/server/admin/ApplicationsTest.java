package com.oauth2cloud.server.admin;

import com.oauth2cloud.server.OAuth2CloudTest;
import com.oauth2cloud.server.hibernate.model.TokenResponse;
import com.oauth2cloud.server.rest.OAuth2Cloud;
import org.testng.annotations.Test;

public class ApplicationsTest extends OAuth2CloudTest {

    @Test
    public void testGet() {
        TokenResponse tr = getToken();

        assert target(OAuth2Cloud.API).path("applications")
            .request()
            .header(AUTH_HEADER, "bearer " +  tr.getAccessToken())
            .get().getStatus() == 200;
    }

}
