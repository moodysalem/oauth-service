package com.oauth2cloud.server.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.oauth2cloud.server.OAuth2CloudTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class PublicApplicationsTest extends OAuth2CloudTest {

    @Test
    public void testGet() {
        Response r = target("api/publicapplications")
                .request()
                .get();
        assert r.getStatus() == 200;
        assert r.readEntity(JsonNode.class).size() == 1;
    }
}
