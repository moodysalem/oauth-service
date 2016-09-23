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
    public static final String API_PATH = "api";
    public static final String OAUTH_PATH = "oauth";

    private static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
    private static final String PERSISTENCE_UNIT_NAME = "oauth-service";
    private static final String DB_MASTER_CHANGELOG_XML_PATH = "db/master-changelog.xml";
    private static final String ENTITY_MANAGER_FACTORY_NAME = "main-em";

    public OAuth2Application() {
        super();

        packages("com.oauth2cloud.server.rest");

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                // this is used to talk to the DB via JPA entity manager
                bindFactory(
                        JAXRSEntityManagerFactory.builder(ENTITY_MANAGER_FACTORY_NAME)
                                .withUrl(EnvironmentConfig.JDBC_CONNECTION_STRING)
                                .withUser(EnvironmentConfig.JDBC_CONNECTION_USERNAME)
                                .withPassword(EnvironmentConfig.JDBC_CONNECTION_PASSWORD)
                                .withPersistenceUnit(PERSISTENCE_UNIT_NAME)
                                .withChangelogFile(DB_MASTER_CHANGELOG_XML_PATH)
                                .withShowSql(EnvironmentConfig.SHOW_HIBERNATE_SQL)
                                .withContext(EnvironmentConfig.LIQUIBASE_CONTEXT)
                                .build()
                ).to(EntityManager.class).in(RequestScoped.class).proxy(true);

                // create the mailer that uses amazon
                final Mailer sesMailer = new Mailer(
                        EnvironmentConfig.SMTP_HOST,
                        EnvironmentConfig.SMTP_PORT,
                        EnvironmentConfig.SMTP_USERNAME,
                        EnvironmentConfig.SMTP_PASSWORD,
                        TransportStrategy.SMTP_TLS
                );

                final Properties addtl = new Properties();
                addtl.put(MAIL_SMTP_STARTTLS_REQUIRED, "true");
                sesMailer.applyProperties(addtl);

                // this is used to send e-mails
                bind(sesMailer).to(Mailer.class);

                // this is used for generating e-mails from freemarker templates
                bind(new EmailTemplateFreemarkerConfiguration()).to(Configuration.class);
            }
        });
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
