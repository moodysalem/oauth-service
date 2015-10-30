package com.oauth2cloud.server.resources.admin;

import com.leaguekit.jaxrs.lib.BaseApplication;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("api")
public class AdminApplication extends BaseApplication {
    public AdminApplication() {
        super();

        packages("com.oauth2cloud.server.resources.admin.resources");
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
