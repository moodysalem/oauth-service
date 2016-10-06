package com.oauth2cloud.server.rest.filter;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Applies X-Frame-Options DENY header to any resource annotated with NoXFrame
 */
@Provider
public class NoXFrameOptionsFeature implements DynamicFeature {
    @Priority(Priorities.HEADER_DECORATOR)
    public static class NoXFrameOptionsFilter implements ContainerResponseFilter {
        public static final String X_FRAME_OPTIONS = "X-Frame-Options",
                DENY = "DENY";

        @Override
        public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext)
                throws IOException {
            containerResponseContext.getHeaders().putSingle(X_FRAME_OPTIONS, DENY);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface NoXFrame {
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        if (resourceInfo.getResourceMethod().isAnnotationPresent(NoXFrame.class) ||
                resourceInfo.getResourceClass().isAnnotationPresent(NoXFrame.class)) {
            featureContext.register(NoXFrameOptionsFilter.class);
        }
    }
}
