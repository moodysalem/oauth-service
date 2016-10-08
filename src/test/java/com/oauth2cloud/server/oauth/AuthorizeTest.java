package com.oauth2cloud.server.oauth;

import com.oauth2cloud.server.OAuth2Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;


public class AuthorizeTest extends OAuth2Test {

    @Test
    public void testBadRequests() {
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
                .queryParam("client_id", CLIENT_ID + "1")
                .queryParam("redirect_uri", "http://localhost:8080")
                .queryParam("response_type", "token")
                .request()
                .get().getStatus() == 400;
    }


    @Test
    public void testAuthorizeEndpoint() {
        final Response auth = target("authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", "https://oauth2cloud.com")
                .queryParam("response_type", "token")
                .request()
                .get();

        assert auth.getStatus() == 200;

        // parse the dom of the successful request
        final Document doc = Jsoup.parse(auth.readEntity(String.class));

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
        assert "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css"
                .equals(ss.get(2).attr("href"));

        // page title
        assert "OAuth2Cloud Log In"
                .equals(head.select("title").text());

    }

}
