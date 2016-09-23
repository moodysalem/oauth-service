package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import freemarker.template.Configuration;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;
import java.util.Properties;

@ApplicationPath("/")
public class OAuth2Application extends BaseApplication {
    public static final String API = "api";
    public static final String OAUTH = "oauth";

    public OAuth2Application() {
        super();

        packages("com.oauth2cloud.server.rest");

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                // this is used to talk to the DB via JPA entity manager
                bindFactory(
                        JAXRSEntityManagerFactory.builder("main-em")
                                .withUrl(System.getProperty("JDBC_CONNECTION_STRING"))
                                .withUser(System.getProperty("JDBC_CONNECTION_USERNAME"))
                                .withPassword(System.getProperty("JDBC_CONNECTION_PASSWORD"))
                                .withPersistenceUnit("oauth-service")
                                .withChangelogFile("db/master-changelog.xml")
                                .withShowSql("true".equalsIgnoreCase(System.getProperty("SHOW_SQL")))
                                .withContext(System.getProperty("LIQUIBASE_CONTEXT", "prod"))
                                .build()
                ).to(EntityManager.class).in(RequestScoped.class).proxy(true);

                // create the mailer that uses amazon
                final Mailer sesMailer = new Mailer(
                        System.getProperty("SMTP_HOST"),
                        getMailPort(),
                        System.getProperty("SMTP_USERNAME"),
                        System.getProperty("SMTP_PASSWORD"),
                        TransportStrategy.SMTP_TLS
                );

                final Properties addtl = new Properties();
                addtl.put("mail.smtp.starttls.required", "true");
                sesMailer.applyProperties(addtl);

                // this is used to send e-mails
                bind(sesMailer).to(Mailer.class);

                // this is used for generating e-mails from freemarker templates
                bind(new EmailTemplateFreemarkerConfiguration()).to(Configuration.class);
            }
        });
    }

    private int getMailPort() {
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

    @Override
    public boolean forceLoadBalancerHTTPS() {
        return true;
    }

    @Override
    public boolean allowCORS() {
        return true;
    }
}
