package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.LoginErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
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
        assert "https://s3.amazonaws.com/oauth2cloud-static-assets/favicon-chain.ico?v=2".equals(fi);

        final Elements ss = head.select("link[rel=\"stylesheet\"]");
        // bootstrap stylesheet
        assert "https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css"
                .equals(ss.first().attr("href"));
        // custom stylesheet
        assert "https://cdnjs.cloudflare.com/ajax/libs/bootswatch/3.3.6/cosmo/bootstrap.min.css"
                .equals(ss.get(1).attr("href"));
        // fontawesome
        assert "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css"
                .equals(ss.get(2).attr("href"));

        // page title includes application name
        assert "OAuth2Cloud Log In".equals(head.select("title").text());
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
                    assert !noError.select("#sent-email-alert").isEmpty();
                }
        );
    }

    @Test
    public void validateSentEmailMessage() {
        // if you submit a log in you should not see the error code any longer
        final Document noError = Jsoup.parse(target("authorize")
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
        assert !noError.select("#sent-email-alert").isEmpty();
    }


}
