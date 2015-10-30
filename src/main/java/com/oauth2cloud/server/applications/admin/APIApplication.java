package com.oauth2cloud.server.applications.admin;

import com.leaguekit.jaxrs.lib.BaseApplication;
import com.oauth2cloud.server.applications.ResourceBinder;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("api")
public class APIApplication extends BaseApplication {
    public APIApplication() {
        super();

        packages("com.oauth2cloud.server.applications.admin");

        register(new ResourceBinder());
    }

    @Override
    public boolean forceHttps() {
        return true;
    }

    @Override
    public boolean allowCORS() {
        return true;
    }
}
