package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Scope;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("scopes")
public class ScopesResource extends BaseEntityResource<Scope> {
    @Override
    public Class<Scope> getEntityClass() {
        return Scope.class;
    }

    @Override
    public boolean canCreate(Scope scope) {
        Application ap = scope.getApplication() != null && scope.getApplication().getId() != null ? em.find(Application.class, scope.getApplication().getId()) : null;
        if (ap == null) {
            return false;
        }
        return ap.getOwner().equals(getUser());
    }

    @Override
    public boolean canEdit(Scope scope) {
        return canCreate(scope);
    }

    @Override
    public boolean canDelete(Scope scope) {
        return canCreate(scope);
    }

    @Override
    protected void validateEntity(List<String> list, Scope scope) {
    }

    @Override
    public void beforeCreate(Scope scope) {

    }

    @Override
    public void beforeEdit(Scope scope, Scope t1) {

    }

    @QueryParam("applicationId")
    Long applicationId;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Scope> root) {
        list.add(cb.equal(root.join("application").get("owner"), getUser()));

        if (applicationId != null) {
            list.add(cb.equal(root.join("application").get("id"), applicationId));
        }
    }

    @Override
    public void afterCreate(Scope scope) {

    }

    @Override
    public void beforeSend(Scope scope) {

    }
}
