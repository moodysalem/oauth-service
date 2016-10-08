package com.oauth2cloud.server;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.moodysalem.jaxrs.lib.test.BaseTest;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.rest.EmailTemplateFreemarkerConfiguration;
import freemarker.template.Configuration;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.email.Email;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import javax.persistence.EntityManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class OAuth2Test extends BaseTest {
    protected static final String AUTH_HEADER = "Authorization";
    protected static final String CLIENT_ID = "6a63c1f1f10df85df6f918d68cb8c13e1e44856f7d861b05cbdd63bf7ea009f4";
    protected static final UUID APPLICATION_ID = UUID.fromString("9966e7e3-ac4f-4d8e-9710-2971450cb504");

    private static JAXRSEntityManagerFactory jrem;

    private final List<Email> sentEmails = new ArrayList<>();

    protected Email getLastEmail() {
        return sentEmails.size() > 0 ? sentEmails.get(sentEmails.size() - 1) : null;
    }

    @Override
    public ResourceConfig getResourceConfig() {
        BaseApplication ba = new BaseApplication() {
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
        ba.packages("com.oauth2cloud.server.rest");

        ba.register(new AbstractBinder() {
            @Override
            protected void configure() {
                if (jrem == null) {
                    jrem = JAXRSEntityManagerFactory.builder("main-em")
                            .withUrl(System.getProperty("localdb") != null ?
                                    String.format("jdbc:mysql://localhost:3306/scratch", System.getProperty("localdb")) :
                                    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
                            )
                            .withUser("root")
                            .withPersistenceUnit("oauth-service")
                            .withChangelogFile("db/changesets/master-changelog.xml")
                            .withShowSql(true)
                            .withContext("test")
                            .build();
                }

                // this is used to talk to the DB via JPA entity manager
                bindFactory(jrem).to(EntityManager.class).in(RequestScoped.class).proxy(true);

                // this is used to send e-mails and record the email to our list of sent e-mails
                final Mailer m = mock(Mailer.class);
                doAnswer(invocationOnMock -> sentEmails.add((Email) invocationOnMock.getArguments()[0]))
                        .when(m).sendMail(any(Email.class));

                bind(m).to(Mailer.class);

                bind(new EmailTemplateFreemarkerConfiguration()).to(Configuration.class);
            }
        });

        return ba;
    }

    public static final String ADMIN_USER = "moody.salem@gmail.com";

    public TokenResponse getToken() {
        return getToken(ADMIN_USER);
    }

    /**
     * This method returns a TokenResponse corresponding to a log in to the admin application
     * from the administrative user
     */
    public TokenResponse getToken(final String email) {
        final Form up = new Form();
        up.param("email", email).param("action", "email");

        final Response loginScreen = target("authorize")
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("response_type", "token")
                .request()
                .post(Entity.form(up));

        assert loginScreen.getStatus() == 302;

        final String location = loginScreen.getHeaderString("Location");

        try {
            final URI u = new URI(location);

            final MultivaluedMap<String, String> values = new MultivaluedHashMap<>();
            final TokenResponse tr = new TokenResponse();
            final String frag = u.getFragment();
            final String[] pcs = frag.split(Pattern.quote("&"));
            for (final String pair : pcs) {
                final String[] nv = pair.split(Pattern.quote("="));
                if (nv.length == 2) {
                    values.putSingle(
                            URLDecoder.decode(nv[0], "UTF-8"),
                            URLDecoder.decode(nv[1], "UTF-8")
                    );
                }
            }
            tr.setAccessToken(values.getFirst("access_token"));
            tr.setScope(values.getFirst("scope"));
            tr.setTokenType(values.getFirst("token_type"));
            tr.setExpiresIn(Long.parseLong(values.getFirst("expires_in")));
            return tr;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

    }
}
