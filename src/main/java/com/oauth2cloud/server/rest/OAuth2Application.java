package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.ClientCredentials;
import freemarker.template.Configuration;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;
import java.util.Properties;
import java.util.UUID;

@ApplicationPath("/")
public class OAuth2Application extends BaseApplication {
    private static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required",
            PERSISTENCE_UNIT_NAME = "oauth-service",
            DB_MASTER_CHANGELOG_XML_PATH = "db/changesets/master-changelog.xml",
            ENTITY_MANAGER_FACTORY_NAME = "main-em";

    public static Properties ENTITY_MANAGER_FACTORY_CONFIG = new Properties();

    static {
        ENTITY_MANAGER_FACTORY_CONFIG.put("org.hibernate.envers.audit_table_suffix", "aud");
        ENTITY_MANAGER_FACTORY_CONFIG.put("org.hibernate.envers.revision_field_name", "rev");
        ENTITY_MANAGER_FACTORY_CONFIG.put("org.hibernate.envers.revision_type_field_name", "rev_info");
    }

    public OAuth2Application() {
        super();
        packages("com.oauth2cloud.server.rest");

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                EncryptedStringConverter.init(Environment.ENCRYPTION_SECRET);

                final JAXRSEntityManagerFactory jrem = JAXRSEntityManagerFactory.builder(ENTITY_MANAGER_FACTORY_NAME)
                        .withUrl(Environment.JDBC_CONNECTION_STRING)
                        .withUser(Environment.JDBC_CONNECTION_USERNAME)
                        .withPassword(Environment.JDBC_CONNECTION_PASSWORD)
                        .withPersistenceUnit(PERSISTENCE_UNIT_NAME)
                        .withChangelogFile(DB_MASTER_CHANGELOG_XML_PATH)
                        .withShowSql(Environment.SHOW_HIBERNATE_SQL)
                        .withContext(Environment.LIQUIBASE_CONTEXT)
                        .withAdditionalProperties(ENTITY_MANAGER_FACTORY_CONFIG)
                        .build();

                initializeDefaultClientCredentials(jrem);

                // this is used to talk to the DB via JPA entity manager
                bindFactory(jrem).to(EntityManager.class).in(RequestScoped.class).proxy(true);

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

    public static void initializeDefaultClientCredentials(final JAXRSEntityManagerFactory jrem) {
        final EntityManager em = jrem.provide();

        try {
            TXHelper.withinTransaction(em, () -> {
                final Client client = em.find(Client.class, UUID.fromString("1489a6d1-5933-4d46-98d0-7e62f37ffc5e"));
                client.setCredentials(
                        new ClientCredentials(
                                "6a63c1f1f10df85df6f918d68cb8c13e1e44856f7d861b05cbdd63bf7ea009f4",
                                "0457a1fe452b7f32e4b84db9db139d9a572bad7599544ac368a079bbe8069714"
                        )
                );
                em.merge(client);
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize client");
        } finally {
            em.close();
        }

    }
}
