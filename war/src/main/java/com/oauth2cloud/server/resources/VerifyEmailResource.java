package com.oauth2cloud.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("verify")
public class VerifyEmailResource extends BaseResource {

    @QueryParam("code")
    private String code;

    @GET
    public Response verifyEmail() {
        return error("Not yet implemented.");
    }

}
