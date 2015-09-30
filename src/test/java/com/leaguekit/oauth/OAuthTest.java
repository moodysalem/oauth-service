package com.leaguekit.oauth;

import com.leaguekit.jaxrs.lib.test.BaseTest;
import org.glassfish.jersey.server.ResourceConfig;

public class OAuthTest extends BaseTest {
    @Override
    public ResourceConfig getResourceConfig() {
        return new OAuthApplication();
    }
}
