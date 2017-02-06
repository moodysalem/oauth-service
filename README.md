# oauth-service
An OAuth 2.0 Software as a Service Platform

# API Documentation
[Powered by swagger](https://docs.oauth2cloud.com)

# Build
`mvn clean package`

# Run locally in Jetty container
`mvn compile jetty:run -Pprofile-with-environment-variables`

# Environment Configuration

```java
public static final String JDBC_CONNECTION_STRING = System.getProperty("JDBC_CONNECTION_STRING"),
            JDBC_CONNECTION_USERNAME = System.getProperty("JDBC_CONNECTION_USERNAME"),
            JDBC_CONNECTION_PASSWORD = System.getProperty("JDBC_CONNECTION_PASSWORD"),
            SMTP_HOST = System.getProperty("SMTP_HOST"),
            SMTP_USERNAME = System.getProperty("SMTP_USERNAME"),
            SMTP_PASSWORD = System.getProperty("SMTP_PASSWORD"),
            LIQUIBASE_CONTEXT = System.getProperty("LIQUIBASE_CONTEXT", "prod"),
            SEND_EMAILS_FROM = System.getProperty("SEND_EMAILS_FROM", "do-not-reply@oauth2cloud.com"),
            ENCRYPTION_SECRET = System.getProperty("ENCRYPTION_SECRET");
```
