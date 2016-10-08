package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.Application_;
import com.oauth2cloud.server.model.db.Scope;
import com.oauth2cloud.server.model.db.Scope_;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Path("scopes")
@TokenFilter.ReadToken
public class ScopesResource extends VersionedEntityResource<Scope> {
    @Override
    public Class<Scope> getEntityClass() {
        return Scope.class;
    }

    @Override
    public boolean canMerge(Scope oldData, Scope newData) {
        final Application ap = newData.getApplication() != null && newData.getApplication().getId() != null ?
                em.find(Application.class, newData.getApplication().getId()) : null;

        return ap != null && ap.getOwner().idMatch(getUser());
    }

    @Override
    public void beforeMerge(Scope oldData, Scope newData) {

    }

    @Override
    public void afterMerge(Scope entity) {

    }

    @Override
    public boolean canDelete(Scope scope) {
        return false;
    }

    @QueryParam("applicationId")
    private Set<UUID> applicationId;

    @Override
    public void getPredicatesFromRequest(List<Predicate> list, Root<Scope> root) {
        list.add(cb.equal(root.join(Scope_.application).get(Application_.owner), getUser()));

        if (applicationId != null) {
            list.add(cb.equal(root.join(Scope_.application).get(Application_.id), applicationId));
        }
    }

    @Override
    public void beforeSend(List<Scope> entity) {

    }

    @Override
    public boolean requiresLogin() {
        return true;
    }
}
