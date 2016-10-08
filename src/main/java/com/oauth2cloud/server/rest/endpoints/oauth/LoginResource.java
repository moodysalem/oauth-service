package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class LoginResource extends BaseResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    @NoXFrameOptionsFeature.NoXFrame
    @Path("login/{code}")
    public Response login(@PathParam("code") final String code) {

        return Response.noContent().build();
    }
}
