package com.oauth2cloud.server.oauth;

import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

public class TokenInfoTest extends AuthorizeTest {

    @Test
    public void testToken() {
        Response ti = target("token").path("info").request().post(Entity.form(new Form().param("application_id", "1").param("token", "abc")));

        assert ti.getStatus() == 200;
    }

}
