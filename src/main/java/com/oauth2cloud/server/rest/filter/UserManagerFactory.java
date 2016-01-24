package com.oauth2cloud.server.rest.filter;

import org.glassfish.hk2.api.Factory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;


public class UserManagerFactory implements Factory<UserManagerFactory.UserManager> {

    @Context
    ContainerRequestContext context;

    @Override
    public UserManager provide() {

        return null;
    }

    @Override
    public void dispose(UserManager userManager) {

    }

    public static class UserManager {

    }
}
