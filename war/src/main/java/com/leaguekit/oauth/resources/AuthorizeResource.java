package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;
import com.leaguekit.oauth.model.Application;
import org.glassfish.jersey.server.mvc.Template;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("authorize")
public class AuthorizeResource extends BaseResource {

    public static class AuthorizationResponse {
        private List<String> configurationErrors;

        public List<String> getConfigurationErrors() {
            return configurationErrors;
        }
        public void addConfigurationError(String error) {
            if (configurationErrors == null) {
                configurationErrors = new ArrayList<>();
            }
            configurationErrors.add(error);
        }
    }

    @GET
    @Template(name = "/templates/Authorize")
    public AuthorizationResponse auth(
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri
    ) {
        AuthorizationResponse ar = new AuthorizationResponse();

        if (clientId == null || redirectUri == null || responseType == null) {
            ar.addConfigurationError("Client ID, redirect URI, and response type are all required for this endpoint.");
        }

        // first look up the Application

        return ar;
    }

    private Application getApplication()

}
