package com.oauth2cloud.server.rest.endpoints.api;


import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

import java.util.UUID;

public class ClientsTest extends OAuth2Test {
    @Test
    public void testCrud() {
        final Crud<Application> appCrud = new Crud<>(Application.class, target("applications"), getToken().getAccessToken());
        final Crud<Client> clientCrud = new Crud<>(Client.class, target("clients"), getToken().getAccessToken());

        final Application app;
        {
            final Application a = new Application();
            a.setName(UUID.randomUUID().toString());
            a.setSupportEmail("moody.salem@gmail.com");
            app = appCrud.save(a);
            assert app.getId() != null;
        }

        final Client client;
        {
            Client c = new Client();
            c.setApplication(app);
            c.setName(UUID.randomUUID().toString());
            assert clientCrud.saveResponse(c).getStatus() == 422;
            c.setTokenTtl(86400L);
            assert clientCrud.saveResponse(c).getStatus() == 422;
            c.setLoginCodeTtl(300);
            c = clientCrud.save(c);
            assert clientCrud.list(new Crud.Param("applicationId", app.getId()))
                    .stream().anyMatch(c::idMatch);
        }


    }
}
