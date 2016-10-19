package com.oauth2cloud.server;

import org.testng.annotations.Test;

public class SwaggerTest extends OAuth2Test {
    @Test
    public void testJson() {
        assert target("swagger.json").request().get().getStatus() == 200;
    }

}
