package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.Application_;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.Scope;
import com.oauth2cloud.server.model.Scope_;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.AuthorizationHeaderTokenFeature;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.UUID;

@AuthorizationHeaderTokenFeature.ReadToken
@Path(OAuth2Application.API + "/scopes")
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

        Application ap = scope.getApplication() != null && scope.getApplication().getId() != null ?
                em.find(Application.class, scope.getApplication().getId()) : null;

        return ap != null && ap.isActive() && ap.getOwner().idMatch(getUser());
    }

    @Override
    public boolean canEdit(Scope scope) {
        mustBeLoggedIn();
        checkScope(MANAGE_SCOPES);
        return scope.getApplication().getOwner().idMatch(getUser());
    }

    @Override
    public boolean canDelete(Scope scope) {
        return false;
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
    UUID applicationId;

    @QueryParam("active")
    Boolean active;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Scope> root) {
        mustBeLoggedIn();
        checkScope(MANAGE_SCOPES);

        list.add(cb.equal(root.join(Scope_.application).get(Application_.owner), getUser()));

        if (applicationId != null) {
            list.add(cb.equal(root.join(Scope_.application).get(Application_.id), applicationId));
        }

        if (active != null) {
            list.add(cb.equal(root.get(Scope_.active), true));
        }
    }

    @Override
    public void afterCreate(Scope scope) {

    }

    @Override
    public void beforeSend(Scope scope) {

    }

}
