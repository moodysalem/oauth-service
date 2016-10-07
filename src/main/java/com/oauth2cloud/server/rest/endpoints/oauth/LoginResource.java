package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@NoXFrameOptionsFeature.NoXFrame
@Path(OAuth2Application.OAUTH_PATH + "/login")
public class LoginResource extends BaseResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response login(@QueryParam("code") final String code) {

        return Response.noContent().build();
    }
}
