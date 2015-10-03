package com.leaguekit.oauth.resources;

import org.glassfish.jersey.server.mvc.Template;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("authorize")
public class AuthorizeResource extends BaseResource {

    public static class AuthorizationResponse {

    }

    @GET
    @Template(name = "/templates/Login")
    public AuthorizationResponse auth(
        @HeaderParam("Referer") String referer,
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri
    ) {
        AuthorizationResponse ar = new AuthorizationResponse();

        return ar;
    }

}
