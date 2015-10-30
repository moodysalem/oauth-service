package com.oauth2cloud.server.applications.oauth.resources;

import com.oauth2cloud.server.applications.BaseResource;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * This responds to the callback from the amazon login page by serving up a basically empty page that posts a message
 * to the parent window allowing the user login form to be submitted
 */
@Path("amazon")
public class AmazonLoginResource extends BaseResource {
    @GET
    public Response getToken() {
        return Response.ok(new Viewable("/templates/AmazonLogin")).build();
    }
}
