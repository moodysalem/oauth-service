package com.leaguekit.oauth;


import com.leaguekit.jaxrs.lib.BaseApplication;
import com.leaguekit.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("")
public class OAuthApplication extends BaseApplication {

    public OAuthApplication() {
        super();

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
                    true,
                    context
                )).to(EntityManager.class).in(RequestScoped.class);
            }
        });

    }

}