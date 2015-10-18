package com.oauth2cloud.server.filter;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class NoXFrameOptions implements ContainerResponseFilter {

    public static final String X_FRAME_OPTIONS = "X-Frame-Options";
    public static final String DENY = "DENY";

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext)
        throws IOException {
        containerResponseContext.getHeaders().putSingle(X_FRAME_OPTIONS, DENY);
    }
}
