package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.db.ClientScope;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

import java.util.UUID;

public class ClientScopesResourceTest extends OAuth2Test {
    @Test
    public void testQueryParams() {
        final Crud<ClientScope> crud = clientScopeCrud(getToken().getAccessToken());

        // just verify they don't cause errors
        assert crud.list(new Crud.Param("clientId", UUID.randomUUID())).isEmpty();
        assert crud.list(new Crud.Param("scopeId", UUID.randomUUID())).isEmpty();
    }
}