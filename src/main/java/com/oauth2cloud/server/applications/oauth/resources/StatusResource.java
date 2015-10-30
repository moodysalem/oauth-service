package com.oauth2cloud.server.applications.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.applications.BaseResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("status")
public class StatusResource extends BaseResource {

    @GET
    public Response status() {
        String OK = (String) em.createNativeQuery("SELECT 'OK' FROM DUAL").getSingleResult();

        if (OK == null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to select OK from database.");
        }

        return Response.ok(OK).build();
    }

}
