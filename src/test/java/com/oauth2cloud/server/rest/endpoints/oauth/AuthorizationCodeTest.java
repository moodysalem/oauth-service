package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.OAuthErrorResponse;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.util.CookieUtil;
import com.oauth2cloud.server.util.Crud;
import org.jsoup.Jsoup;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class AuthorizationCodeTest extends OAuth2Test {

    @Test
    public void testAuthorizationCode() {
        final TokenResponse tr = getToken("test@gmail.com");
        final Crud<Application> ca = applicationCrud(tr.getAccessToken());
        final Crud<Client> cc = clientCrud(tr.getAccessToken());
        final Crud<ClientScope> csc = clientScopeCrud(tr.getAccessToken());
        final Crud<Scope> sc = scopeCrud(tr.getAccessToken());

        // create an application
        Application application = new Application();
        application.setName(UUID.randomUUID().toString());
        application.setSupportEmail("test@gmail.com");
        application = ca.save(application);

        // create some scopes
        Scope getFriends = new Scope();
        getFriends.setName("get_friends");
        getFriends.setApplication(application);
        getFriends.setDisplayName("Get Friends");
        getFriends = sc.save(getFriends);

        Scope getProfile = new Scope();
        getProfile.setName("get_profile");
        getProfile.setDisplayName("Get Profile");
        getProfile.setApplication(application);
        getProfile = sc.save(getProfile);

        // save them both
        sc.save(Arrays.asList(getFriends, getProfile));

        // Create a client
        Client client = new Client();
        client.setApplication(application);
        client.setFlows(Collections.singleton(GrantFlow.CODE));
        client.setTokenTtl(86400);
        client.setLoginCodeTtl(300);
        client.setName("Authorization Code");
        client.setUris(Collections.singleton("https://moodysalem.com"));
        client = cc.save(client);

        assert target("authorize")
                .queryParam("response_type", "token")
                .queryParam("client_id", client.getCredentials().getId())
                .queryParam("redirect_uri", "https://moodysalem.com")
                .request().get().getStatus() == 400;

        assert target("authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getCredentials().getId())
                .queryParam("redirect_uri", "https://johndoe.com")
                .request().get().getStatus() == 400;

        // now use the client to get an authorization
        assert target("authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getCredentials().getId())
                .queryParam("redirect_uri", "https://moodysalem.com")
                .request()
                .get().getStatus() == 200;

        assert target("authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getCredentials().getId())
                .queryParam("redirect_uri", "https://moodysalem.com/abc-123/red/blue")
                .request()
                .get().getStatus() == 200;

        final String code = getAccessCode("https://moodysalem.com/abc-123", client, "moody.salem@gmail.com");

        // cannot get token information on a code
        assert target("token").path("info")
                .request()
                .post(
                        Entity.form(
                                new Form()
                                        .param("token", code)
                                        .param("client_id", client.getCredentials().getId())
                        )
                ).getStatus() == 400;

        final WebTarget exchange = target("token").path("authorization_code");

        {
            // requires code
            assert exchange.request()
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("client_id", client.getCredentials().getId())
                                            .param("redirect_uri", "https://moodysalem.com/abc-123")
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_request);

            // requires redirect_uri
            assert exchange.request()
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", code)
                                            .param("client_id", client.getCredentials().getId())
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_request);

            // requires client_id
            assert exchange.request()
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", code)
                                            .param("redirect_uri", "https://moodysalem.com/abc-123")
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_request);

            final TokenResponse exc = exchange.request()
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", code)
                                            .param("redirect_uri", "https://moodysalem.com/abc-123")
                                            .param("client_id", client.getCredentials().getId())
                            ),
                            TokenResponse.class
                    );

            // it has already been used
            assert exchange.request()
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", code)
                                            .param("redirect_uri", "https://moodysalem.com/abc-123")
                                            .param("client_id", client.getCredentials().getId())
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_grant);

            assert exc.getUser().getEmail().equals("moody.salem@gmail.com");
            assert isBlank(exc.getScope());
            assert exc.getClientId().equals(client.getCredentials().getId());
            assert Math.abs(exc.getExpiresIn() - client.getTokenTtl()) < 10;
        }

        // now make the client credential and then ensure that authentication is required
        client.setConfidential(true);
        client = cc.save(client);

        {
            final String moody = getAccessCode("https://moodysalem.com/123-abc", client, "moody@moodysalem.com");

            // requires client authentication
            assert exchange.request()
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", moody)
                                            .param("redirect_uri", "https://moodysalem.com/123-abc")
                                            .param("client_id", client.getCredentials().getId())
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_client);

            assert exchange.request()
                    .header("Authorization", "Basic abc-123")
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", moody)
                                            .param("redirect_uri", "https://moodysalem.com/abc-123")
                                            .param("client_id", client.getCredentials().getId())
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_client);

            // auth'd with invalid redirect uri
            assert basicAuth(exchange.request(), client)
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", moody)
                                            // wrong domain
                                            .param("redirect_uri", "https://hoodiehoo.com/abc-123")
                                            .param("client_id", client.getCredentials().getId())
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_grant);

            // auth'd with invalid redirect uri
            assert basicAuth(exchange.request(), client)
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", moody)
                                            // wrong pathname
                                            .param("redirect_uri", "https://moodysalem.com/abc-123")
                                            .param("client_id", client.getCredentials().getId())
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_grant);

            // proper authentication
            assert basicAuth(exchange.request(), client)
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", moody)
                                            // wrong pathname
                                            .param("redirect_uri", "https://moodysalem.com/123-abc")
                                            .param("client_id", client.getCredentials().getId())
                            ),
                            TokenResponse.class
                    ).getUser().getEmail().equals("moody@moodysalem.com");

            // already used
            assert basicAuth(exchange.request(), client)
                    .post(
                            Entity.form(
                                    new Form()
                                            .param("code", moody)
                                            // wrong pathname
                                            .param("redirect_uri", "https://moodysalem.com/123-abc")
                                            .param("client_id", client.getCredentials().getId())
                            )
                    ).readEntity(OAuthErrorResponse.class).getError().equals(OAuthErrorResponse.Type.invalid_grant);
        }
    }

    private String getAccessCode(final String redirectUri, final Client client, final String email) {
        final URI uri;
        try {
            uri = new URI(redirectUri);
        } catch (URISyntaxException e) {
            return null;
        }
        // log the user in
        final Response login = target("authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getCredentials().getId())
                .queryParam("redirect_uri", uri.toString())
                .request()
                .post(
                        Entity.form(
                                new Form()
                                        .param("email", email)
                                        .param("remember_me", "on")
                                        .param("action", "email")
                        )
                );

        assert login.getStatus() == 200;
        final String link = Jsoup.parse(lastSentEmail().getTextHTML()).select("#login-link").attr("href");
        assert !isBlank(link);

        final Response completion = client().target(link).request().get();
        assert completion.getStatus() == 302;
        assert completion.getCookies().keySet().contains(CookieUtil.COOKIE_NAME_PREFIX + client.getApplication().getId());
        assert completion.getLocation().getHost().equals(uri.getHost());
        assert completion.getLocation().getPath().equals(uri.getPath());

        final String code;
        {
            code = Stream.of(
                    completion.getLocation().getQuery().split("&")
            ).filter(p -> p.startsWith("code="))
                    .map(p -> p.split("=")[1])
                    .findFirst().orElse(null);
        }
        assert code != null;

        return code;
    }
}
