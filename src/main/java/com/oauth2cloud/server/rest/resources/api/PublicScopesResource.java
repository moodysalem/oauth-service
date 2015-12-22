package com.oauth2cloud.server.rest.resources.api;

import com.oauth2cloud.server.hibernate.model.Scope;
import com.oauth2cloud.server.rest.models.PublicScope;
import com.oauth2cloud.server.rest.resources.BaseEntityResource;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class PublicScopesResource extends BaseEntityResource<Scope> {
    @Override
    public Class<Scope> getEntityClass() {
        return Scope.class;
    }

    @Override
    public boolean canCreate(Scope entity) {
        return false;
    }

    @Override
    public boolean canEdit(Scope entity) {
        return false;
    }

    @Override
    public boolean canDelete(Scope entity) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> errors, Scope entity) {

    }

    @Override
    public void beforeCreate(Scope entity) {

    }

    @Override
    public void beforeEdit(Scope oldEntity, Scope entity) {

    }

    @QueryParam("applicationId")
    Long applicationId;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> predicates, Root<Scope> root) {
        mustBeLoggedIn();

        predicates.add(cb.equal(root.join("application").get("deleted"), false));
        predicates.add(cb.equal(root.join("application").get("publicClientRegistration"), true));

        if (applicationId != null) {
            predicates.add(cb.equal(root.join("application").get("id"), applicationId));
        }
    }

    @Override
    public void afterCreate(Scope entity) {

    }

    @Override
    public void beforeSend(Scope entity) {

    }

    @Override
    public Response getList() {
        Response r = super.getList();
        if (!r.hasEntity() || !(r.getEntity() instanceof List)) {
            return r;
        }
        return Response.fromResponse(r)
            // replace the entity in the response with public application instances
            .entity(((List<Scope>) r.getEntity()).stream().map(PublicScope::new).collect(Collectors.toList()))
            .build();
    }

    @Override
    public Response get(long id) {
        Response r = super.get(id);
        if (!r.hasEntity() || !(r.getEntity() instanceof Scope)) {
            return r;
        }
        return Response.fromResponse(r).entity(new PublicScope((Scope) r.getEntity())).build();
    }


}
