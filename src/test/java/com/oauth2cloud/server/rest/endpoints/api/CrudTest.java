package com.oauth2cloud.server.rest.endpoints.api;


import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

import java.util.*;

public class CrudTest extends OAuth2Test {
    @Test
    public void testCrudApplications() {
        final TokenResponse response = getToken();
        final Crud<Application> crud = applicationCrud(response.getAccessToken());

        final Application app = new Application();
        assert crud.saveResponse(app).getStatus() == 422;
        app.setName(UUID.randomUUID().toString());
        assert crud.saveResponse(app).getStatus() == 422;

        app.setSupportEmail("moody.salem@gmail.com");
        assert crud.saveResponse(app).getStatus() == 200;
        assert crud.saveResponse(app).getStatus() == 422;
        app.setName(null);
        assert crud.saveResponse(app).getStatus() == 422;
        app.setName(UUID.randomUUID().toString());

        assert crud.delete(crud.save(app)).getStatus() == 204;

        Application saved = crud.save(app);
        saved.setGoogleCredentials(null);
        assert crud.saveResponse(saved).getStatus() == 200;

        saved.setGoogleCredentials(new ClientCredentials("abc", "123"));
        assert crud.saveResponse(saved).getStatus() == 200;
        saved = crud.get(saved.getId());
        assert saved.getGoogleCredentials() != null;
        assert saved.getGoogleCredentials().getId().equals("abc");
        assert saved.getGoogleCredentials().getSecret().equals("123");

        saved.setGoogleCredentials(new ClientCredentials("abc", null));
        assert crud.saveResponse(saved).getStatus() == 422;

        saved.setGoogleCredentials(new ClientCredentials(null, "abc"));
        assert crud.saveResponse(saved).getStatus() == 422;
    }

    @Test
    public void testApplicationWorkflow() {
        final String token = getToken().getAccessToken();
        final Crud<Application> appCrud = applicationCrud(token);
        final Crud<Client> clientCrud = clientCrud(token);
        final Crud<Scope> scopeCrud = scopeCrud(token);
        final Crud<ClientScope> csCrud = clientScopeCrud(token);

        final Application app;
        {
            Application a = new Application();
            a.setName(UUID.randomUUID().toString());
            a.setSupportEmail("moody.salem@gmail.com");
            a = appCrud.save(a);
            a = appCrud.get(a.getId());
            app = appCrud.save(a);
            assert app.getId() != null;
        }

        final Set<Scope> scopes = new HashSet<>();
        {
            // test validation
            Scope v = new Scope();
            assert scopeCrud.saveResponse(v).getStatus() == 403;
            v.setApplication(app);
            assert scopeCrud.saveResponse(v).getStatus() == 422;
            v.setName(" tes spaces");
            assert scopeCrud.saveResponse(v).getStatus() == 422;
            v.setName("no_spaces");
            assert scopeCrud.saveResponse(v).getStatus() == 422;
            v.setDisplayName("can have spaces");
            v = scopeCrud.save(v);
            v = scopeCrud.get(v.getId());
            assert scopeCrud.delete(v).getStatus() == 204;
            assert scopeCrud.delete(new Crud.Param("applicationId", app.getId())).getStatus() == 204;

            for (final String name : new String[]{"one_scope", "two_scope", "three_scope"}) {
                final Scope s = new Scope();
                s.setName(name);
                s.setApplication(app);
                s.setDisplayName(name);
                scopes.add(scopeCrud.save(s));
            }


        }

        final Client client;
        {
            Client c = new Client();
            c.setApplication(app);
            c.setName(UUID.randomUUID().toString());
            assert clientCrud.saveResponse(c).getStatus() == 422;
            c.setTokenTtl(86400L);
            assert clientCrud.saveResponse(c).getStatus() == 422;
            c.setLoginCodeTtl(30);
            assert clientCrud.saveResponse(c).getStatus() == 422;
            c.setLoginCodeTtl(300);
            c = clientCrud.save(c);
            assert clientCrud.list(new Crud.Param("applicationId", app.getId()))
                    .stream().anyMatch(c::idMatch);
            assert c.getCredentials().getId().length() > 32 && c.getCredentials().getSecret().length() > 32;
            assert c.getFlows() == null || c.getFlows().isEmpty();
            c.setFlows(Collections.singleton(GrantFlow.CLIENT_CREDENTIALS));
            c = clientCrud.save(c);
            assert c.getFlows().contains(GrantFlow.CLIENT_CREDENTIALS) && c.getFlows().size() == 1;
            c.setConfidential(true);
            c = clientCrud.save(c);
            assert c.isConfidential();

            client = c;
        }

        // create the client scopes
        {
            final List<ClientScope> create = new LinkedList<>();
            for (final Scope scope : scopes) {
                final ClientScope cs = new ClientScope();
                cs.setClient(client);
                cs.setPriority(ClientScope.Priority.REQUIRED);
                cs.setReason("Need this!");
                cs.setScope(scope);
                create.add(cs);
            }
            final List<ClientScope> list = csCrud.save(create);
            assert list.size() == create.size();
        }
    }
}
