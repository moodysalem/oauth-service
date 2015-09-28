package com.leaguekit.oauth;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

public class BaseApplication extends ResourceConfig {
    public BaseApplication() {
        // register the things that are typically used by a JAX-RS application
        register(JacksonFeature.class);

        EncodingFilter.enableFor(this, GZipEncoder.class);
    }
}