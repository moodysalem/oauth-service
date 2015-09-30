package com.leaguekit.oauth.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("authorize")
public class AuthorizeResource extends BaseResource {

    @GET
    public Response authorizeEndpoint() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

}
