package com.oauth2cloud.server.admin;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.hibernate.model.TokenResponse;
import com.oauth2cloud.server.rest.OAuth2Application;
import org.testng.annotations.Test;

public class PublicApplicationsTest extends OAuth2Test {

    @Test
    public void testGet() {
        TokenResponse tr = getToken();

        assert target(OAuth2Application.API).path("publicapplications")
            .request()
            .header(AUTH_HEADER, "bearer " + tr.getAccessToken())
            .get().getStatus() == 200;
    }

}
