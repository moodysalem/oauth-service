package com.oauth2cloud.server.rest.resources.oauth;

import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * This responds to the callback from the amazon login page by serving up an empty page that uses HTML5 postMessage
 * for the parent window allowing the user login form to be submitted with the amazon token
 */
@NoXFrameOptionsFeature.NoXFrame
@Path(OAuth2Application.OAUTH + "/amazon")
public class AmazonLoginResource extends OAuthResource {
    @GET
    public Response getToken() {
        return Response.ok(new Viewable("/templates/AmazonLogin")).build();
    }
}
