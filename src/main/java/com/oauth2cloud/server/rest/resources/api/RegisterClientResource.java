package com.oauth2cloud.server.rest.resources.api;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Scope;
import com.oauth2cloud.server.rest.OAuth2Cloud;
import com.oauth2cloud.server.rest.filter.TokenFeature;
import com.oauth2cloud.server.rest.models.PublicApplication;
import com.oauth2cloud.server.rest.models.PublicScope;
import com.oauth2cloud.server.rest.models.RegisterClientInfo;
import com.oauth2cloud.server.rest.resources.BaseResource;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This resource has all the endpoints that are required for
 */
@TokenFeature.ReadToken
@Path(OAuth2Cloud.API + "/registerclient")
public class RegisterClientResource extends BaseResource {

    /**
     * Get the information needed for the register client screen
     *
     * @param applicationId the application id for which the info is required
     * @return the info required for the registration screen
     */
    @GET
    public Response getApplicationData(@QueryParam("applicationId") Long applicationId) {
        if (applicationId == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                "Application ID is a required query parameter.");
        }

        Application app = em.find(Application.class, applicationId);

        if (app == null || !app.isPublicClientRegistration() || !app.isActive()) {
            throw new RequestProcessingException(Response.Status.NOT_FOUND, "A public application with the provided ID could not be found.");
        }

        RegisterClientInfo rci = new RegisterClientInfo();
        rci.setApplication(new PublicApplication(app));
        rci.setScopes(getPublicScopes(app));

        return Response.ok(rci).build();
    }

    /**
     * Get a list of scopes that a client can request for an app
     *
     * @param app the app to get scopes for
     * @return a list of scopes
     */
    private List<PublicScope> getPublicScopes(Application app) {
        CriteriaQuery<Scope> scopes = cb.createQuery(Scope.class);

        Root<Scope> root = scopes.from(Scope.class);

        scopes.select(root).where(
            cb.equal(root.get("application"), app),
            cb.equal(root.get("requestable"), true)
        );

        List<Scope> scopeList = em.createQuery(scopes).getResultList();
        return scopeList.stream().map(PublicScope::new).collect(Collectors.toList());
    }

}
