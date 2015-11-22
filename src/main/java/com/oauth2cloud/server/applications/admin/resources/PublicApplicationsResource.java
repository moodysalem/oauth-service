package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.applications.admin.models.PublicApplication;
import com.oauth2cloud.server.hibernate.model.Application;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gets the applications but wraps them all in PublicApplication instances
 */
@Path("publicapplications")
public class PublicApplicationsResource extends BaseEntityResource<Application> {

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public Response getList() {
        Response r = super.getList();
        if (!r.hasEntity() || !(r.getEntity() instanceof List)) {
            return r;
        }
        return Response.fromResponse(r)
            // replace the entity in the response with public application instances
            .entity(((List<Application>) r.getEntity()).stream().map(PublicApplication::new).collect(Collectors.toList()))
            .build();
    }

    @Override
    public Response get(long id) {
        Response r = super.get(id);
        if (!r.hasEntity() || !(r.getEntity() instanceof Application)) {
            return r;
        }
        return Response.fromResponse(r).entity(new PublicApplication((Application) r.getEntity())).build();
    }

    @Override
    public Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    public boolean canCreate(Application entity) {
        return false;
    }

    @Override
    public boolean canEdit(Application entity) {
        return false;
    }

    @Override
    public boolean canDelete(Application entity) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> errors, Application entity) {

    }

    @Override
    public void beforeCreate(Application entity) {

    }

    @Override
    public void beforeEdit(Application oldEntity, Application entity) {

    }

    @QueryParam("name")
    String name;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> predicates, Root<Application> root) {
        predicates.add(cb.equal(root.get("publicClientRegistration"), true));

        // allow searching for applications by name
        if (name != null) {
            String so = name.trim();
            Predicate toAdd = null;

            for (String s : so.split(" ")) {
                if (!s.isEmpty()) {
                    Predicate wordSearch = cb.like(root.get("name"), "%" + s + "%");
                    toAdd = (toAdd == null) ? wordSearch : cb.and(toAdd, wordSearch);
                }
            }

            if (toAdd != null) {
                predicates.add(toAdd);
            }
        }
    }

    @Override
    public void afterCreate(Application entity) {

    }

    @Override
    public void beforeSend(Application entity) {

    }
}
