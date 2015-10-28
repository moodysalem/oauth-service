package com.oauth2cloud.server.resources;

import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("amazon")
public class AmazonLoginResource extends BaseResource {
    @GET
    public Response getToken() {
        return Response.ok(new Viewable("/templates/AmazonLogin")).build();
    }
}
