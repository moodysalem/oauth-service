package com.oauth2cloud.server;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.moodysalem.jaxrs.lib.test.BaseTest;
import com.oauth2cloud.server.hibernate.model.TokenResponse;
import com.oauth2cloud.server.rest.OAuth2Application;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
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
            public boolean forceHttps() {
                return true;
            }

            @Override
            public boolean allowCORS() {
                return true;
            }
        };

        ba.packages("com.oauth2cloud.server.rest");

        ba.register(new AbstractBinder() {
            @Override
            protected void configure() {
                if (jrem == null) {
                    jrem = new JAXRSEntityManagerFactory(
                            "jdbc:mysql://localhost:3306/oauthtest",
                            "root", "",
                            "oauth-service",
                            "db/master-changelog.xml",
                            // show sql
                            true,
                            // validate
                            true,
                            // migration context
                            "test",
                            null
                    );
                }

                // this is used to talk to the DB via JPA entity manager
                bindFactory(jrem).to(EntityManager.class).in(RequestScoped.class).proxy(true);

                // this is used to send e-mails
                Mailer m = mock(Mailer.class);
                doAnswer(invocationOnMock -> sentEmails.add((Email) invocationOnMock.getArguments()[0]))
                        .when(m)
                        .sendMail(any());
                bind(m).to(Mailer.class);

                // this is used for generating e-mails from freemarker templates
                Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);
                freemarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates/email"));
                freemarkerConfiguration.setDefaultEncoding("UTF-8");
                bind(freemarkerConfiguration).to(Configuration.class);
            }
        });

        System.setProperty("ENCRYPTION_SECRET", "xTUf4mP2SI6nfeLO");

        return ba;
    }

    /**
     * This method returns a TokenResponse corresponding to a log in to the admin application
     * from the administrative user
     */
    public TokenResponse getToken() {

        Form up = new Form();
        up.param("email", "moody.salem@gmail.com")
                .param("password", "moody")
                .param("action", "login");

        Response loginScreen = target(OAuth2Application.OAUTH)
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .path("authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("response_type", "token")
                .request()
                .post(Entity.form(up));

        assert loginScreen.getStatus() == 302;

        String loc = loginScreen.getHeaderString("Location");

        try {
            URI u = new URI(loc);

            MultivaluedMap<String, String> values = new MultivaluedHashMap<>();
            TokenResponse tr = new TokenResponse();
            String frag = u.getFragment();
            String[] pcs = frag.split(Pattern.quote("&"));
            for (String pair : pcs) {
                String[] nv = pair.split(Pattern.quote("="));
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
