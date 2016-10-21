package com.oauth2cloud.server;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.moodysalem.jaxrs.lib.test.BaseTest;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.rest.EmailTemplateFreemarkerConfiguration;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.util.TokenUtil;
import freemarker.template.Configuration;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.email.Email;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.oauth2cloud.server.rest.OAuth2Application.configureSwagger;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class OAuth2Test extends BaseTest implements SendsMail {
    public static final String AUTH_HEADER = "Authorization",
            CLIENT_ID = "l56ladN92ryWSpsamIkGQduwvdRk3K7J1RNS6x6tZ34dVs2HKHMyO7G4lqIHxUrV7N9KxGKuYJAFXWJSKw1rKu458agHnorM";

    public static final UUID APPLICATION_ID = UUID.fromString("9966e7e3-ac4f-4d8e-9710-2971450cb504");

    private static JAXRSEntityManagerFactory jrem;

    @Override
    public Email lastSentEmail() {
        return sentEmails.size() > 0 ? sentEmails.get(sentEmails.size() - 1) : null;
    }

    @Override
    public List<Email> getSentEmails() {
        return Collections.unmodifiableList(sentEmails);
    }

    private final List<Email> sentEmails = new LinkedList<>();

    @Override
    public ResourceConfig getResourceConfig() {
        final BaseApplication app = new BaseApplication() {
            @Override
            public boolean forceLoadBalancerHTTPS() {
                return false;
            }

            @Override
            public boolean allowCORS() {
                return true;
            }
        };

        EncryptedStringConverter.init("xTUf4mP2SI6nfeLO");
        app.packages("com.oauth2cloud.server.rest");
        configureSwagger(app);

        app.register(new AbstractBinder() {
            @Override
            protected void configure() {
                if (jrem == null) {
                    jrem = JAXRSEntityManagerFactory.builder("main-em")
                            .withUrl(System.getProperty("localdb") != null ?
                                    String.format("jdbc:mysql://localhost:3306/%s", System.getProperty("localdb")) :
                                    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
                            )
                            .withUser("root")
                            .withPersistenceUnit("oauth-service")
                            .withChangelogFile("db/changesets/master-changelog.xml")
                            .withShowSql(true)
                            .withContext("test")
                            .withAdditionalProperties(OAuth2Application.ENTITY_MANAGER_FACTORY_CONFIG)
                            .build();
                }

                OAuth2Application.initializeDefaultClientCredentials(jrem);

                // this is used to talk to the DB via JPA entity manager
                bindFactory(jrem).to(EntityManager.class).in(RequestScoped.class).proxy(true);

                final Mailer mailer = mock(Mailer.class);
                // this is used to send e-mails and record the email to our list of sent e-mails
                doAnswer(
                        invocation -> {
                            final Email email = (Email) invocation.getArguments()[0];
                            sentEmails.add(email);
                            return null;
                        }
                ).when(mailer).sendMail(any(Email.class));
                bind(mailer).to(Mailer.class);

                bind(new EmailTemplateFreemarkerConfiguration()).to(Configuration.class);
            }
        });

        return app;
    }

    public static final String ADMIN_USER = "moody.salem@gmail.com";

    public TokenResponse getToken() {
        return TokenUtil.getToken(client(), this, ADMIN_USER);
    }

}
