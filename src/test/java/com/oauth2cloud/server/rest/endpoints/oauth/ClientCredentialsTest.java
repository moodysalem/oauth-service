package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.OAuthErrorResponse;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ClientCredentialsTest extends OAuth2Test {
    @Test
    public void testClientCredentials() {
        final String token = getToken().getAccessToken();
        final Crud<Application> ac = applicationCrud(token);
        final Crud<Client> cc = clientCrud(token);
        final Crud<Scope> sc = scopeCrud(token);
        final Crud<ClientScope> csc = clientScopeCrud(token);

        // create an app
        Application t = new Application();
        t.setName(UUID.randomUUID().toString());
        t.setSupportEmail("moody.salem@gmail.com");
        t = ac.save(t);

        // create a client
        Client client = new Client();
        client.setName("confidential client credentials client");
        client.setFlows(Collections.singleton(GrantFlow.CLIENT_CREDENTIALS));
        client.setTokenTtl(3600L);
        client.setApplication(t);
        client.setLoginCodeTtl(300);
        client = cc.save(client);

        final WebTarget endpoint = target("token").path("client_credentials");

        // no authorization header
        {
            final OAuthErrorResponse err = endpoint.request().post(Entity.form(new Form())).readEntity(OAuthErrorResponse.class);
            assert err.getError().equals(OAuthErrorResponse.Type.invalid_client);
        }

        // authorization header
        {
            final OAuthErrorResponse err = basicAuth(endpoint.request(), client)
                    .post(Entity.form(new Form())).readEntity(OAuthErrorResponse.class);
            assert err.getError().equals(OAuthErrorResponse.Type.unauthorized_client);
        }

        client.setConfidential(true);
        client = cc.save(client);
        assert client.isConfidential();

        // authorization header
        {
            final TokenResponse tr = basicAuth(endpoint.request(), client)
                    .post(Entity.form(new Form())).readEntity(TokenResponse.class);

            assert Math.abs(tr.getExpiresIn() - client.getTokenTtl()) < 20;
            assert tr.getAccessToken() != null;
            assert tr.getUser() == null;
            assert tr.getClientId().equals(client.getCredentials().getId());
            assert isBlank(tr.getScope());
        }

        // remove flow from client
        client.setFlows(Collections.emptySet());
        client = cc.save(client);
        assert client.getFlows().isEmpty();

        {
            final OAuthErrorResponse err = basicAuth(endpoint.request(), client)
                    .post(Entity.form(new Form())).readEntity(OAuthErrorResponse.class);
            assert err.getError().equals(OAuthErrorResponse.Type.unauthorized_client);
        }

        // add some client scopes to it
        Scope scope = new Scope();
        scope.setApplication(t);
        scope.setDisplayName("Test Display Name");
        scope.setName("hello_world");
        scope = sc.save(scope);

        ClientScope cs = new ClientScope();
        cs.setScope(scope);
        cs.setClient(client);
        cs.setReason("Because");
        cs.setPriority(ClientScope.Priority.REQUIRED);
        cs = csc.save(cs);

        client.setFlows(Collections.singleton(GrantFlow.CLIENT_CREDENTIALS));
        client = cc.save(client);

        {
            final Response r = basicAuth(endpoint.request(), client)
                    .post(Entity.form(new Form()));

            // no cache headers
            assert r.getHeaderString("Cache-Control").equals("no-store");
            assert r.getHeaderString("Pragma").equals("no-cache");

            final TokenResponse tr = r.readEntity(TokenResponse.class);

            assert tr.getScope().contains(scope.getName());
        }

        {
            final OAuthErrorResponse invalidScopes = basicAuth(endpoint.request(), client)
                    .post(Entity.form(new Form().param("scope", "abc")))
                    .readEntity(OAuthErrorResponse.class);
            assert invalidScopes.getError().equals(OAuthErrorResponse.Type.invalid_scope);
        }

        // additional scopes but requesting a particular scope
        Scope other = new Scope();
        other.setApplication(t);
        other.setDisplayName("other");
        other.setName("test_me");
        other = sc.save(other);


        ClientScope otherCs = new ClientScope();
        otherCs.setScope(other);
        otherCs.setClient(client);
        otherCs.setReason("Because");
        otherCs.setPriority(ClientScope.Priority.ASK);
        otherCs = csc.save(otherCs);

        {
            final TokenResponse testMe = basicAuth(endpoint.request(), client)
                    .post(Entity.form(new Form().param("scope", "test_me")))
                    .readEntity(TokenResponse.class);
            assert testMe.getScope().contains("test_me") && !testMe.getScope().contains("hello_world");

            final TokenResponse helloWorld = basicAuth(endpoint.request(), client)
                    .post(Entity.form(new Form().param("scope", "hello_world")))
                    .readEntity(TokenResponse.class);
            assert !helloWorld.getScope().contains("test_me") && helloWorld.getScope().contains("hello_world");
        }
    }
}
