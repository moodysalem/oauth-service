package com.oauth2cloud.server.admin;

import com.oauth2cloud.server.applications.admin.models.PublicApplication;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

public class ApplicationsTest extends AdminTest {

    @Test
    public void testGet() {
        Response r = target("publicapplications").request().header(AUTH_HEADER, TOKEN).get();
        assert r.getStatus() == 200;
        assert r.readEntity(new GenericType<List<PublicApplication>>() {
        }).size() == 1;
    }
}
