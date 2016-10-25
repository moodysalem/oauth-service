package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import io.swagger.annotations.Api;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import java.util.List;

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

    @Override
    public void getPredicatesFromRequest(List<Predicate> predicates, Root<ClientScope> root) {
        predicates.add(cb.equal(root.join(ClientScope_.client).join(Client_.application).get(Application_.owner), getUser()));
    }

    @Override
    public void beforeSend(List<ClientScope> entity) {

    }

    @Override
    public boolean requiresLogin() {
        return true;
    }


}
