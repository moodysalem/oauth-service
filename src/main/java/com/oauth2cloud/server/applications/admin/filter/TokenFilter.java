package com.oauth2cloud.server.applications.admin.filter;

import com.oauth2cloud.server.applications.oauth.OAuthApplication;
import com.oauth2cloud.server.applications.oauth.resources.TokenResource;
import com.oauth2cloud.server.hibernate.model.TokenResponse;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class TokenFilter implements ContainerRequestFilter {

    public static final String BEARER = "bearer ";
    public static final String TOKEN = "TOKEN";
    public static final String CLIENT_ID = System.getProperty("CLIENT_ID");

    private static final Logger LOG = Logger.getLogger(TokenFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String auth = containerRequestContext.getHeaderString("Authorization");
        if (auth != null && auth.toLowerCase().startsWith(BEARER)) {
            String token = auth.substring(BEARER.length());
            if (token.length() > 0) {
                try {
                    URI tokenInfoEndpoint = containerRequestContext.getUriInfo().getRequestUriBuilder()
                            .replacePath(OAuthApplication.OAUTH)
                            .path(TokenResource.class)
                            .path(TokenResource.class.getMethod("tokenInfo", String.class, String.class))
                            .build();

                    Form f = (new Form()).param("token", token).param("client_id", CLIENT_ID);
                    Response r = ClientBuilder.newClient().target(tokenInfoEndpoint).request().post(Entity.form(f));
                    if (r.getStatus() == 200) {
                        TokenResponse tr = r.readEntity(TokenResponse.class);
                        if (CLIENT_ID.equals(tr.getClientId())) {
                            containerRequestContext.setProperty(TOKEN, tr);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    LOG.log(Level.WARNING, "Token exception", e);
                }
            }
        }
    }
}
