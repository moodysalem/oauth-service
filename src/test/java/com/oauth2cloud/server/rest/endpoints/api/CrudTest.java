package com.oauth2cloud.server.rest.endpoints.api;


import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.api.UserInfo;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.util.Crud;
import com.oauth2cloud.server.util.TokenUtil;
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
    public void testUserLinking() {
        final String token = getToken().getAccessToken();
        final Crud<Application> ac = applicationCrud(token);
        final Crud<User> uc = userCrud(token);
        final Crud<Client> cc = clientCrud(token);

        Application a = new Application();
        a.setName(UUID.randomUUID().toString());
        a.setSupportEmail("moody.salem@gmail.com");
        a = ac.save(a);

        User one = new User();
        one.setEmail("moody.salem@gmail.com");
        one.setApplication(a);
        one = uc.save(one);
        assert one.getId() != null;

        User two = new User();
        two.setEmail("moody.salem+yahoo@gmail.com");
        two.setApplication(a);
        two = uc.save(two);
        assert two.getId() != null;

        two.setGroup(new UserGroup());
        two = uc.save(two);
        assert two.getGroup().getId() != null;

        one.setGroup(two.getGroup());
        one = uc.save(one);
        assert one.getGroup().idMatch(two.getGroup());
        assert one.getGroup().getUsers().size() == 2;

        {
            Application b = new Application();
            b.setName(UUID.randomUUID().toString());
            b.setSupportEmail("other@gmail.com");
            b = ac.save(b);

            User bthree = new User();
            bthree.setApplication(b);
            bthree.setEmail("moody.salem@gmail.com");
            bthree = uc.save(bthree);
            assert bthree.getId() != null;

            bthree.setGroup(one.getGroup());
            assert uc.saveResponse(bthree).getStatus() == 403;
        }

        User three = new User();
        three.setEmail("moody.salem@gmail.com");
        three.setApplication(a);
        assert uc.saveResponse(three).getStatus() == 422;
        three.setEmail("moody+test@gmail.com");
        three = uc.save(three);
        assert three.getGroup() == null;
        three.setGroup(new UserGroup());
        three.getGroup().setId(one.getGroup().getId());
        three = uc.save(three);
        assert three.getGroup() != null && three.getGroup().getUsers().size() == 3;

        Client atc = new Client();
        atc.setConfidential(false);
        atc.setUris(Collections.singleton("http://localhost:8080"));
        atc.setLoginCodeTtl(3600);
        atc.setName(UUID.randomUUID().toString());
        atc.setTokenTtl(3600L);
        atc.setFlows(Collections.singleton(GrantFlow.IMPLICIT));
        atc.setApplication(a);
        atc = cc.save(atc);
        assert atc.getId() != null;

        final TokenResponse tr = getToken("moody+test@gmail.com", atc.getCredentials().getId(), atc.getUris().iterator().next());
        assert tr != null;
        final TokenResponse ti = TokenUtil.tokenInfo(target(), tr.getAccessToken(), atc.getApplication().getId());
        assert ti != null;

        assert ti.getUser().getLinkedUsers().size() == 2;
        {
            final UserInfo uio = UserInfo.from(one), uit = UserInfo.from(two);
            assert ti.getUser().getLinkedUsers().stream().anyMatch(uio::equals);
            assert ti.getUser().getLinkedUsers().stream().anyMatch(uit::equals);
        }
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
