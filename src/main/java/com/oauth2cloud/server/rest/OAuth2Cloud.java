package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.BaseApplication;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class OAuth2Cloud extends BaseApplication{
    public static final String API = "api";
    public static final String OAUTH = "oauth";

    public OAuth2Cloud() {
        super();

        packages("com.oauth2cloud.server.rest");

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
