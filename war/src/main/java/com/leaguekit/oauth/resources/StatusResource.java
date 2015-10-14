package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("status")
public class StatusResource extends BaseResource {

    @GET
    public Response status() {
        Integer integer = (Integer) em.createNativeQuery("SELECT 1 FROM DUAL").getSingleResult();

        if (integer == null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to select 1 from database.");
        }

        return Response.ok().build();
    }

    @Override
    protected boolean usesSessions() {
        return false;
    }
}
