package com.oauth2cloud.server.admin;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class PublicApplicationsTest extends AdminTest {

    @Test
    public void testGet() {
        Response r = target("publicapplications").request().header(AUTH_HEADER, TOKEN).get();
        assert r.getStatus() == 200;
        assert r.readEntity(JsonNode.class).size() == 1;
    }
}
