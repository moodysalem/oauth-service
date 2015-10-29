package com.oauth2cloud.server;

import com.leaguekit.jaxrs.lib.test.BaseTest;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Base test for all the other tests
 */
public class OAuthTest extends BaseTest {
    @Override
    public ResourceConfig getResourceConfig() {
        System.setProperty("JDBC_CONNECTION_STRING", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("JDBC_CONNECTION_USERNAME", "sa");
        System.setProperty("JDBC_CONNECTION_PASSWORD", "sa");

        System.setProperty("SMTP_HOST", "localhost");
        System.setProperty("SMTP_USERNAME", "fake");
        System.setProperty("SMTP_PASSWORD", "fake");

        System.setProperty("ENCRYPTION_SECRET", "xTUf4mP2SI6nfeLO");

        return new OAuthApplication();
    }
}
