package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import org.codemonkey.simplejavamail.Mailer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;

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
                bindFactory(new JAXRSEntityManagerFactory(
                    System.getProperty("JDBC_CONNECTION_STRING"),
                    System.getProperty("JDBC_CONNECTION_USERNAME"),
                    System.getProperty("JDBC_CONNECTION_PASSWORD"),
                    "oauth-service",
                    "db/master-changelog.xml",
                    System.getProperty("DEBUG") != null,
                    System.getProperty("LIQUIBASE_CONTEXT", "")
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
                bind(
                    new Mailer(
                        System.getProperty("SMTP_HOST"),
                        port,
                        System.getProperty("SMTP_USERNAME"),
                        System.getProperty("SMTP_PASSWORD")
                    )
                ).to(Mailer.class);

                // this is used for generating e-mails from freemarker templates
                Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);
                freemarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates/email"));
                freemarkerConfiguration.setDefaultEncoding("UTF-8");
                bind(freemarkerConfiguration).to(Configuration.class);
            }
        });
    }

    @Override
    public boolean forceHttps() {
        return true;
    }

    @Override
    public boolean allowCORS() {
        return true;
    }
}
