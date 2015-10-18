package com.leaguekit.oauth;


import com.leaguekit.jaxrs.lib.BaseApplication;
import com.leaguekit.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.leaguekit.jaxrs.lib.factories.MailSessionFactory;
import com.leaguekit.oauth.filter.NoXFrameOptions;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.mail.Session;
import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("")
public class OAuthApplication extends BaseApplication {

    public OAuthApplication() {
        super();
        register(NoXFrameOptions.class);

        packages("com.leaguekit.oauth.resources");

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                String context = System.getProperty("LIQUIBASE_CONTEXT");
                if (context == null) {
                    context = "";
                }
                bindFactory(new JAXRSEntityManagerFactory(
                    System.getProperty("JDBC_CONNECTION_STRING"),
                    System.getProperty("JDBC_CONNECTION_USERNAME"),
                    System.getProperty("JDBC_CONNECTION_PASSWORD"),
                    "oauth-service",
                    "db/master-changelog.xml",
                    System.getProperty("SHOW_SQL") != null,
                    context
                )).to(EntityManager.class).in(RequestScoped.class);


                int port = 25;
                if (System.getProperty("SMTP_PORT") != null) {
                    try {
                        port = Integer.parseInt(System.getProperty("SMTP_PORT"));
                    } catch (NumberFormatException ignored) {
                    }
                }

                bindFactory(
                    new MailSessionFactory(
                        System.getProperty("SMTP_HOST"),
                        System.getProperty("SMTP_USERNAME"),
                        System.getProperty("SMTP_PASSWORD"),
                        port
                    )
                ).to(Session.class).in(RequestScoped.class);


                Configuration fmConfig = new Configuration(Configuration.VERSION_2_3_23);
                fmConfig.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates/email"));
                fmConfig.setDefaultEncoding("UTF-8");
                bind(fmConfig).to(Configuration.class);
            }
        });

    }

}