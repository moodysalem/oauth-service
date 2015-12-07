package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.Scope;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("scopes")
public class ScopesResource extends BaseEntityResource<Scope> {
    public static final String MANAGE_SCOPES = "manage_scopes";

    @Override
    public Class<Scope> getEntityClass() {
        return Scope.class;
    }

    @Override
    public boolean canCreate(Scope scope) {
        mustBeLoggedIn();
        checkScope(MANAGE_SCOPES);
        Application ap = scope.getApplication() != null && scope.getApplication().getId() != 0 ? em.find(Application.class, scope.getApplication().getId()) : null;
        if (ap == null) {
            return false;
        }
        return ap.getOwner().equals(getUser());
    }

    @Override
    public boolean canEdit(Scope scope) {
        mustBeLoggedIn();
        checkScope(MANAGE_SCOPES);
        return scope.getApplication().getOwner().equals(getUser());
    }

    @Override
    public boolean canDelete(Scope scope) {
        return canEdit(scope);
    }

    @Override
    protected void validateEntity(List<String> list, Scope scope) {
    }

    @Override
    public void beforeCreate(Scope scope) {
        if (scope.getThumbnail() != null && scope.getThumbnail().trim().isEmpty()) {
            scope.setThumbnail(null);
        }
    }

    @Override
    public void beforeEdit(Scope scope, Scope t1) {
        beforeCreate(t1);
    }

    @QueryParam("applicationId")
    Long applicationId;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Scope> root) {
        mustBeLoggedIn();
        checkScope(MANAGE_SCOPES);

        list.add(cb.equal(root.join("application").get("owner"), getUser()));
        list.add(cb.equal(root.get("deleted"), false));

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

    @Override
    protected void deleteEntity(Scope entityToDelete) {
        entityToDelete.setDeleted(true);
        em.merge(entityToDelete);
    }
}
