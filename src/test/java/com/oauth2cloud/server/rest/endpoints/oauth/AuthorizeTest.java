package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.LoginErrorCode;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.util.Crud;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AuthorizeTest extends OAuth2Test {

    @Test
    public void testBadRequests() {
        // no query parameters
        assert target("authorize")
                .request()
                .get().getStatus() == 400;

        // code flow is not allowed for this client
        assert target("authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "http://localhost:8080")
                .queryParam("response_type", "code")
                .request()
                .get().getStatus() == 400;

        // invalid redirect uri
        assert target("authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "http://localhost:8081")
                .queryParam("response_type", "token")
                .request()
                .get().getStatus() == 400;


        // invalid client id
        assert target("authorize")
                .queryParam("client_id", "ABC")
                .queryParam("redirect_uri", "http://localhost:8080")
                .queryParam("response_type", "token")
                .request()
                .get().getStatus() == 400;
    }


    @Test
    public void validateCustomization() {
        final Response auth = target("authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("response_type", "token")
                .request()
                .get();

        assert auth.getStatus() == 200;
        assert auth.getHeaderString("X-Frame-Options").equals("DENY");

        // parse the dom of the successful request
        final Document doc = Jsoup.parse(auth.readEntity(String.class));

        // verify favicon is in there
        final Element head = doc.select("head").first();
        final String fi = head.select("link[rel=\"icon\"]").first().attr("href");
        assert "https://s3.amazonaws.com/oauth2cloud-static-assets/favicon-logo.ico?v=3".equals(fi);

        final Elements ss = head.select("link[rel=\"stylesheet\"]");

        assert ss.stream().map(s -> s.attr("href")).collect(Collectors.toSet())
                .containsAll(
                        Arrays.asList(
                                //bootstrap
                                "https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/css/bootstrap.min.css",
                                //paper
                                "https://cdnjs.cloudflare.com/ajax/libs/bootswatch/3.3.7/paper/bootstrap.min.css",
                                //fontawesome
                                "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css"
                        )
                );

        // page title includes application name
        assert "Administration - OAuth2Cloud".equals(head.select("title").text());
        assert "Administration".equals(doc.select("h2 small").text());
    }

    @Test
    public void testErrorCodes() {
        Stream.of(LoginErrorCode.values()).forEach(
                code -> {
                    // an error code in the url indicates something went wrong
                    final Document error = Jsoup.parse(target("authorize")
                            .queryParam("client_id", CLIENT_ID)
                            .queryParam("redirect_uri", "https://oauth2cloud.com")
                            .queryParam("response_type", "token")
                            .queryParam("error_code", code.name())
                            .request()
                            .get(String.class));
                    assert error.select("#error-code-alert").text().contains(code.getMessage());

                    // if you submit a log in you should not see the error code any longer
                    final Document noError = Jsoup.parse(target("authorize")
                            .queryParam("client_id", CLIENT_ID)
                            .queryParam("redirect_uri", "https://oauth2cloud.com")
                            .queryParam("response_type", "token")
                            .queryParam("error_code", code.name())
                            .request()
                            .post(
                                    Entity.form(
                                            new Form().param("email", "moody.salem@gmail.com")
                                                    .param("action", "email")
                                    ),
                                    String.class
                            ));
                    assert noError.select("#error-code-alert").isEmpty();
                }
        );
    }

    @Test
    public void testShowPromptNoScopes() {
        final String token = getToken().getAccessToken();
        final Crud<Application> ac = applicationCrud(token);
        final Crud<Client> cc = clientCrud(token);
        final Crud<ClientScope> csc = clientScopeCrud(token);
        final Crud<Scope> sc = scopeCrud(token);

        Application a = new Application();
        a.setName(UUID.randomUUID().toString());
        a.setSupportEmail("moody.salem@gmail.com");
        a = ac.save(a);

        Client c = new Client();
        c.setFlows(Collections.singleton(GrantFlow.IMPLICIT));
        c.setUris(Collections.singleton("https://localhost:3000"));
        c.setShowPromptNoScopes(false);
        c.setName("Hello World");
        c.setApplication(a);
        c.setTokenTtl(86400);
        c.setLoginCodeTtl(300);
        c = cc.save(c);

        Scope s1 = new Scope();
        s1.setApplication(a);
        s1.setDisplayName("s1");
        s1.setName("s1");
        s1.setDescription("test scope");
        s1 = sc.save(s1);

        Scope s2 = new Scope();
        s2.setApplication(a);
        s2.setDisplayName("s2");
        s2.setName("s2");
        s2.setDescription("test scope");
        s2 = sc.save(s2);

        ClientScope cs1 = new ClientScope();
        cs1.setClient(c);
        cs1.setScope(s1);
        cs1.setPriority(ClientScope.Priority.ASK);
        cs1.setReason("because");
        cs1 = csc.save(cs1);

        assert target("authorize").queryParam("client_id", c.getCredentials().getId())
                .queryParam("response_type", "token")
                .queryParam("redirect_uri", "https://localhost:3000")
                .request().get().getStatus() == 200;

        assert target("authorize").queryParam("client_id", c.getCredentials().getId())
                .queryParam("response_type", "token")
                .queryParam("redirect_uri", "https://localhost:3000")
                .request().post(
                        Entity.form(
                                new Form()
                                        .param("email", "test@gmail.com")
                                        .param("action", "email")
                        )
                ).getStatus() == 200;
        final Document emailContent = Jsoup.parse(lastSentEmail().getTextHTML());
        final String loginLink = emailContent.select("#login-link").attr("href");

        final Response permis = client().target(loginLink).request().get();
        assert permis.getStatus() == 200;

        final Document permissions = Jsoup.parse(permis.readEntity(String.class));
        // we need to see the checkbox
        assert permissions.select("input[name][type=checkbox]").attr("name")
                .contains(cs1.getId().toString());
    }

    @Test
    public void validateSentEmailMessage() {
        // if you submit a log in you should not see the error code any longer
        final Document sentEmail = Jsoup.parse(target("authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("response_type", "token")
                .request()
                .post(
                        Entity.form(
                                new Form().param("email", "moody.salem@gmail.com")
                                        .param("action", "email")
                        ),
                        String.class
                ));

        assert !sentEmail.select("#sent-email-alert").isEmpty();
    }

    @Test
    public void validateGoogleCredentialsShowsButton() {
        final String token = getToken().getAccessToken();
        final Crud<Application> ac = applicationCrud(token);
        final Crud<Client> cc = clientCrud(token);

        Application a = new Application();
        a.setName(UUID.randomUUID().toString());
        a.setSupportEmail("moody.saleM@gmail.com");
        a = ac.save(a);
        assert a.getGoogleCredentials() == null;

        Client c = new Client();
        c.setApplication(a);
        c.setFlows(Collections.singleton(GrantFlow.IMPLICIT));
        c.setName(UUID.randomUUID().toString());
        c.setTokenTtl(86400);
        c.setConfidential(false);
        c.setLoginCodeTtl(300);
        c.setUris(Collections.singleton("http://google.com"));
        c = cc.save(c);
        assert c.getId() != null;

        final Document noGoogle = Jsoup.parse(
                target("authorize")
                        .queryParam("client_id", c.getCredentials().getId())
                        .queryParam("redirect_uri", "http://google.com")
                        .queryParam("response_type", "token")
                        .request()
                        .get(String.class)
        );

        assert noGoogle.select("#google-login").size() == 0;

        final ClientCredentials gc = new ClientCredentials(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        a.setGoogleCredentials(gc);
        a = ac.save(a);
        assert a.getGoogleCredentials().equals(gc);

        final Document hasGoogle = Jsoup.parse(
                target("authorize")
                        .queryParam("client_id", c.getCredentials().getId())
                        .queryParam("redirect_uri", "http://google.com")
                        .queryParam("response_type", "token")
                        .request()
                        .get(String.class)
        );

        assert hasGoogle.select("button#google-login").size() == 1;
    }


}
