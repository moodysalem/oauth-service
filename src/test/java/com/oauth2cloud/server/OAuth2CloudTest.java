package com.oauth2cloud.server;

import com.oauth2cloud.server.rest.OAuth2Cloud;
import org.glassfish.jersey.server.ResourceConfig;

public class OAuth2CloudTest extends com.moodysalem.jaxrs.lib.test.BaseTest {
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN = "bearer abc";

    @Override
    public ResourceConfig getResourceConfig() {
        System.setProperty("JDBC_CONNECTION_STRING", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("JDBC_CONNECTION_USERNAME", "sa");
        System.setProperty("JDBC_CONNECTION_PASSWORD", "sa");
        System.setProperty("LIQUIBASE_CONTEXT", "test");

        System.setProperty("SMTP_HOST", "localhost");
        System.setProperty("SMTP_USERNAME", "fake");
        System.setProperty("SMTP_PASSWORD", "fake");

        System.setProperty("ENCRYPTION_SECRET", "xTUf4mP2SI6nfeLO");

        return new OAuth2Cloud();
    }
}
