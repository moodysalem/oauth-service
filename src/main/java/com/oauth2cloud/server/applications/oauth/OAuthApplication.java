package com.oauth2cloud.server.applications.oauth;


import com.leaguekit.jaxrs.lib.BaseApplication;
import com.oauth2cloud.server.applications.ResourceBinder;
import com.oauth2cloud.server.applications.oauth.filter.NoXFrameOptions;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("oauth")
public class OAuthApplication extends BaseApplication {

    public OAuthApplication() {
        super();
        register(NoXFrameOptions.class);

        packages("com.oauth2cloud.server.applications.oauth");

        register(new ResourceBinder());

    }

    @Override
    public boolean forceHttps() {
        return true;
    }

    @Override
    public boolean allowCORS() {
        return false;
    }

}