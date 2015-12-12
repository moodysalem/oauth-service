package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.moodysalem.jaxrs.lib.factories.MailSessionFactory;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.mail.Session;
import javax.persistence.EntityManager;

/**
 * Registers a factory for entity managers and sending e-mails and parsing freemarker
 * templates
 */
public class ResourceBinder extends AbstractBinder {
    @Override
    protected void configure() {
        String context = System.getProperty("LIQUIBASE_CONTEXT", "");

        // this is used to talk to the DB via JPA entity manager
        bindFactory(new JAXRSEntityManagerFactory(
                System.getProperty("JDBC_CONNECTION_STRING"),
                System.getProperty("JDBC_CONNECTION_USERNAME"),
                System.getProperty("JDBC_CONNECTION_PASSWORD"),
                "oauth-service",
                "db/master-changelog.xml",
                System.getProperty("DEBUG") != null,
                context
        )).to(EntityManager.class).in(RequestScoped.class).proxy(true);

        // get the port configuration
        int port = 25;
        if (System.getProperty("SMTP_PORT") != null) {
            try {
                port = Integer.parseInt(System.getProperty("SMTP_PORT"));
            } catch (NumberFormatException ignored) {
            }
        }

        // this is used to send e-mails
        bindFactory(
                new MailSessionFactory(
                        System.getProperty("SMTP_HOST"),
                        System.getProperty("SMTP_USERNAME"),
                        System.getProperty("SMTP_PASSWORD"),
                        port
                )
        ).to(Session.class).in(RequestScoped.class);

        // this is used for generating e-mails from freemarker templates
        Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);
        freemarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates/email"));
        freemarkerConfiguration.setDefaultEncoding("UTF-8");
        bind(freemarkerConfiguration).to(Configuration.class);
    }
}
