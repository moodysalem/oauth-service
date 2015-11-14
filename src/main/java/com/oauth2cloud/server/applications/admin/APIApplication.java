package com.oauth2cloud.server.applications.admin;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.oauth2cloud.server.applications.ResourceBinder;
import com.oauth2cloud.server.applications.admin.filter.TokenFilter;

import javax.ws.rs.ApplicationPath;

@ApplicationPath(APIApplication.API)
public class APIApplication extends BaseApplication {

    public static final String API = "api";

    public APIApplication() {
        super();

        packages("com.oauth2cloud.server.applications.admin");

        register(new ResourceBinder());
        register(TokenFilter.class);
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
