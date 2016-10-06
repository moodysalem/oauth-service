package com.oauth2cloud.server.rest;

/**
 * All environment variables are stored in static public members of this class
 */
public class Environment {
    public static final String JDBC_CONNECTION_STRING = System.getProperty("JDBC_CONNECTION_STRING"),
            JDBC_CONNECTION_USERNAME = System.getProperty("JDBC_CONNECTION_USERNAME"),
            JDBC_CONNECTION_PASSWORD = System.getProperty("JDBC_CONNECTION_PASSWORD"),
            SMTP_HOST = System.getProperty("SMTP_HOST"),
            SMTP_USERNAME = System.getProperty("SMTP_USERNAME"),
            SMTP_PASSWORD = System.getProperty("SMTP_PASSWORD"),
            LIQUIBASE_CONTEXT = System.getProperty("LIQUIBASE_CONTEXT", "prod"),
            SEND_EMAILS_FROM = System.getProperty("SEND_EMAILS_FROM", "admin@oauth2cloud.com");

    public static final int SMTP_PORT = getSmtpPort();
    public static final boolean SHOW_HIBERNATE_SQL = "true".equalsIgnoreCase(System.getProperty("SHOW_SQL"));


    private static int getSmtpPort() {
        // get the port configuration
        int port = 25;
        String portString = System.getProperty("SMTP_PORT");
        if (portString != null) {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException ignored) {
            }
        }
        return port;
    }
}
