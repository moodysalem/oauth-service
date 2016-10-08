package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
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
    private static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required",
            PERSISTENCE_UNIT_NAME = "oauth-service",
            DB_MASTER_CHANGELOG_XML_PATH = "db/changesets/master-changelog.xml",
            ENTITY_MANAGER_FACTORY_NAME = "main-em";

    public OAuth2Application() {
        super();


        packages("com.oauth2cloud.server.rest");

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                EncryptedStringConverter.init(Environment.ENCRYPTION_SECRET);

                // this is used to talk to the DB via JPA entity manager
                bindFactory(
                        JAXRSEntityManagerFactory.builder(ENTITY_MANAGER_FACTORY_NAME)
                                .withUrl(Environment.JDBC_CONNECTION_STRING)
                                .withUser(Environment.JDBC_CONNECTION_USERNAME)
                                .withPassword(Environment.JDBC_CONNECTION_PASSWORD)
                                .withPersistenceUnit(PERSISTENCE_UNIT_NAME)
                                .withChangelogFile(DB_MASTER_CHANGELOG_XML_PATH)
                                .withShowSql(Environment.SHOW_HIBERNATE_SQL)
                                .withContext(Environment.LIQUIBASE_CONTEXT)
                                .build()
                ).to(EntityManager.class).in(RequestScoped.class).proxy(true);

                // create the mailer that uses amazon
                final Mailer mailer = new Mailer(
                        Environment.SMTP_HOST,
                        Environment.SMTP_PORT,
                        Environment.SMTP_USERNAME,
                        Environment.SMTP_PASSWORD,
                        TransportStrategy.SMTP_TLS
                );

                final Properties mailerProperties = new Properties();
                mailerProperties.put(MAIL_SMTP_STARTTLS_REQUIRED, "true");
                mailer.applyProperties(mailerProperties);

                // this is used to send e-mails
                bind(mailer).to(Mailer.class);

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
