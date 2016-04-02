package com.oauth2cloud.server.rest.resources.api;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Scope;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.AuthorizationHeaderTokenFeature;
import com.oauth2cloud.server.rest.models.PublicApplication;
import com.oauth2cloud.server.rest.models.PublicScope;
import com.oauth2cloud.server.rest.models.RegisterClientInfo;
import com.oauth2cloud.server.rest.models.RegisterClientRequest;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AuthorizationHeaderTokenFeature.ReadToken
@Path(OAuth2Application.API + "/publicapplications")
public class PublicApplicationsResource extends BaseEntityResource<Application> {

    @Override
    public Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    public boolean canCreate(Application application) {
        return false;
    }

    @Override
    public boolean canEdit(Application application) {
        return false;
    }

    @Override
    public boolean canDelete(Application application) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> list, Application application) {

    }

    @Override
    public void beforeCreate(Application application) {

    }

    @Override
    public void beforeEdit(Application application, Application t1) {

    }

    @Override
    public Response getList() {
        Response r = super.getList();

        if (r.getStatus() == 200) {
            return Response.fromResponse(r).entity(
                    ((List<Application>) (r.getEntity())).stream().map(PublicApplication::new).collect(Collectors.toList())
            ).build();
        }

        return r;
    }

    @Override
    public Response get(UUID id) {
        Response r = super.get(id);

        if (r.getStatus() == 200) {
            return Response.fromResponse(r).entity(
                    new PublicApplication((Application) r.getEntity())
            ).build();
        }

        return r;
    }

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Application> root) {

    }

    @Override
    public void afterCreate(Application application) {

    }

    @Override
    public void beforeSend(Application application) {

    }

    /**
     * Get the information needed for the register client screen
     *
     * @param applicationId the application id for which the info is required
     * @return the info required for the registration screen
     */
    @GET
    @Path("{id}/info")
    public Response getRegistrationInfo(@PathParam("id") UUID applicationId) {
        PublicApplication app = new PublicApplication(getApplication(applicationId));

        RegisterClientInfo rci = new RegisterClientInfo();
        rci.setPublicApplication(app);
        rci.setPublicScopes(getScopes(app.getId()).stream().map(PublicScope::new).collect(Collectors.toList()));

        return Response.ok(rci).build();
    }

    /**
     * Register a client
     *
     * @param applicationId id of application registered for
     * @param req           the request body
     * @return no content
     */
    @POST
    @Path("{id}/register")
    public Response makeClient(@PathParam("id") Long applicationId, RegisterClientRequest req) {
        if (req == null || req.getClient() == null || req.getClientScopes() == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Client and scopes are required");
        }

        return Response.noContent().build();
    }

    /**
     * Get an application for this resource
     *
     * @param applicationId id of applicatino
     * @return application if visible
     */
    private Application getApplication(UUID applicationId) {
        Application app = em.find(Application.class, applicationId);

        if (app == null || !app.isPublicClientRegistration() || !app.isActive()) {
            throw new RequestProcessingException(Response.Status.NOT_FOUND,
                    "A public application with the provided ID could not be found.");
        }

        return app;
    }

    /**
     * Get a list of scopes that a client can request for an app
     *
     * @param applicationId the ID of the application to get the list of scopes for
     * @return a list of scopes
     */
    private List<Scope> getScopes(UUID applicationId) {
        CriteriaQuery<Scope> scopes = cb.createQuery(Scope.class);

        Root<Scope> root = scopes.from(Scope.class);

        scopes.select(root).where(
                cb.equal(root.join("application").get("id"), applicationId),
                cb.equal(root.get("requestable"), true),
                cb.equal(root.get("active"), true)
        );

        return em.createQuery(scopes).getResultList();
    }

}
