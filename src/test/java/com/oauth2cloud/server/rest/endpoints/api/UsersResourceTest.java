package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

public class UsersResourceTest extends OAuth2Test {

    @Test
    public void testQueryParams() {
        final Crud<User> userCrud = userCrud(getToken().getAccessToken());


        userCrud.list(new Crud.Param("grouped", true)).stream().allMatch(u -> u.getGroup() != null);
        userCrud.list(new Crud.Param("grouped", false)).stream().allMatch(u -> u.getGroup() == null);

        userCrud.list(new Crud.Param("applicationId", APPLICATION_ID))
                .stream().allMatch(u -> u.getApplication().getId().equals(APPLICATION_ID));
    }
}