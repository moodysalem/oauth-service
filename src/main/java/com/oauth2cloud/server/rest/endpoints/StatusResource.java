package com.oauth2cloud.server.rest.endpoints;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.model.api.StatusResponse;
import com.oauth2cloud.server.model.api.Version;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("status")
public class StatusResource {
    public static final String DB_CHANGELOG_COUNT = "SELECT COUNT(1) FROM DATABASECHANGELOG";
    @Inject
    private EntityManager em;

    @GET
    @Produces("application/json")
    public Response status() {
        final Number databaseVersion;
        try {
            databaseVersion = (Number) em.createNativeQuery(DB_CHANGELOG_COUNT).getSingleResult();
        } catch (Exception e) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Failed to get database version");
        }

        return Response.ok(
                new StatusResponse(databaseVersion.longValue(), Version.get())
        ).build();
    }
}
