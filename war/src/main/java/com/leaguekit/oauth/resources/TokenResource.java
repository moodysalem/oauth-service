package com.leaguekit.oauth.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("token")
public class TokenResource extends BaseResource {

    @GET
    public Response tokenInquiry() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

}
