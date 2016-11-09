package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.db.Scope;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

import java.util.UUID;

public class ScopesResourceTest extends OAuth2Test {

    @Test
    public void testQueryParams() {
        final Crud<Scope> scopeCrud = scopeCrud(getToken().getAccessToken());

        assert scopeCrud.list(new Crud.Param("applicationId", UUID.randomUUID())).isEmpty();
        assert !scopeCrud.list(new Crud.Param("applicationId", APPLICATION_ID)).isEmpty();
        assert !scopeCrud.list(new Crud.Param("applicationId", UUID.randomUUID(), APPLICATION_ID)).isEmpty();
    }

}