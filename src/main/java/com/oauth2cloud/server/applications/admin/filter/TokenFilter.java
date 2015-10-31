package com.oauth2cloud.server.applications.admin.filter;

import com.oauth2cloud.server.applications.oauth.resources.TokenResource;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

@Provider
public class TokenFilter implements ContainerRequestFilter {

    public static final String BEARER = "bearer ";

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String auth = containerRequestContext.getHeaderString("Authorization");
        if (auth != null && auth.toLowerCase().startsWith(BEARER)) {
            String token = auth.substring(BEARER.length());
            if (token.length() > 0) {
                URI tokenInfoEndpoint = containerRequestContext.getUriInfo().getRequestUriBuilder()
                        .replacePath("oauth").path(TokenResource.class).build();
            }
        }
    }
}
