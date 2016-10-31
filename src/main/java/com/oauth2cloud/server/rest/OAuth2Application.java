package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import com.oauth2cloud.server.model.api.Version;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.ClientCredentials;
import com.oauth2cloud.server.rest.util.GoogleTokenValidator;
import freemarker.template.Configuration;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

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

    public static void configureSwagger(final ResourceConfig resourceConfig) {
        resourceConfig.register(ApiListingResource.class);
        resourceConfig.register(SwaggerSerializers.class);

        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(Version.get());
        beanConfig.setTitle("OAuth2Cloud API");
        beanConfig.setSchemes(new String[]{"https"});
        beanConfig.setBasePath("/");
        beanConfig.setResourcePackage("com.oauth2cloud.server.rest.endpoints");
        beanConfig.setScan(true);
        beanConfig.setDescription("An API for an OAuth2 Server as a service");
        beanConfig.setContact("moody.salem@gmail.com");
        beanConfig.setHost("api.oauth2cloud.com");
    }

    public OAuth2Application() {
        super();
        packages("com.oauth2cloud.server.rest");
        configureSwagger(this);

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

                bind(new GoogleTokenValidator()).to(GoogleTokenValidator.class);
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
                                "l56ladN92ryWSpsamIkGQduwvdRk3K7J1RNS6x6tZ34dVs2HKHMyO7G4lqIHxUrV7N9KxGKuYJAFXWJSKw1rKu458agHnorM",
                                "nxe3ljGyPZizHpKDgvYj9BMbNDlUXqGMH3TyRCcqpivZHxiH3mTYlRZF4X39Wz1dFM3ua5F2c2Uu3TWJA4SzbXwmkwoNfhwo"
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
