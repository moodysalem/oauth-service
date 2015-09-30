package com.leaguekit.oauth;

import com.leaguekit.jaxrs.lib.test.BaseTest;
import org.glassfish.jersey.server.ResourceConfig;

public class OAuthTest extends BaseTest {
    @Override
    public ResourceConfig getResourceConfig() {
        System.setProperty("JDBC_CONNECTION_STRING", "jdbc:h2:mem:test");
        System.setProperty("JDBC_CONNECTION_USERNAME", "sa");
        System.setProperty("JDBC_CONNECTION_PASSWORD", "sa");

        return new OAuthApplication();
    }
}
