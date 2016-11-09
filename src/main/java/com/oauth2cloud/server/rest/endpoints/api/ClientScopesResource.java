package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import io.swagger.annotations.Api;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Api("crud")
@Path("client-scopes")
@TokenFilter.ReadToken
public class ClientScopesResource extends VersionedEntityResource<ClientScope> {
    @Override
    public Class<ClientScope> getEntityClass() {
        return ClientScope.class;
    }

    @Override
    public boolean canMerge(ClientScope oldData, ClientScope newData) {
        if (oldData == null) {
            return newData.getClient() != null && newData.getClient().getId() != null &&
                    em.find(Client.class, newData.getClient().getId()).getApplication().getOwner().idMatch(getUser());
        } else {
            return oldData.getClient().getApplication().getOwner().idMatch(getUser());
        }
    }

    @Override
    public void beforeMerge(ClientScope oldData, ClientScope newData) {

    }

    @Override
    public void afterMerge(ClientScope entity) {

    }

    @Override
    public boolean canDelete(ClientScope toDelete) {
        return canMerge(toDelete, null);
    }

    @QueryParam("clientId")
    private Set<UUID> clientIds;

    @QueryParam("scopeId")
    private Set<UUID> scopeIds;

    @Override
    public void getPredicatesFromRequest(List<Predicate> predicates, Root<ClientScope> root) {
        predicates.add(
                cb.equal(root.join(ClientScope_.client).join(Client_.application).get(Application_.owner), getUser())
        );

        if (clientIds != null && !clientIds.isEmpty()) {
            predicates.add(root.join(ClientScope_.client).get(Client_.id).in(clientIds));
        }

        if (scopeIds != null && !scopeIds.isEmpty()) {
            predicates.add(root.join(ClientScope_.scope).get(Scope_.id).in(scopeIds));
        }
    }

    @Override
    public void beforeSend(List<ClientScope> entity) {

    }

    @Override
    public void checkAccess(Action action) {
        requireScope("manage_client_scopes");
    }
}
