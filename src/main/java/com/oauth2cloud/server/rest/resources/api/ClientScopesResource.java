package com.oauth2cloud.server.rest.resources.api;

import com.oauth2cloud.server.hibernate.model.*;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.AuthorizationHeaderTokenFeature;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.UUID;

@AuthorizationHeaderTokenFeature.ReadToken
@Path(OAuth2Application.API + "/clientscopes")
public class ClientScopesResource extends BaseEntityResource<ClientScope> {

    public static final String MANAGE_CLIENT_SCOPES = "manage_client_scopes";

    @Override
    public Class<ClientScope> getEntityClass() {
        return ClientScope.class;
    }

    @Override
    public boolean canCreate(ClientScope clientScope) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENT_SCOPES);
        Client c = null;
        if (clientScope.getClient() != null && clientScope.getClient().getId() != null) {
            c = em.find(Client.class, clientScope.getClient().getId());
        }
        return c != null && c.isActive() && c.getApplication().isActive() &&
                c.getApplication().getOwner().idMatch(getUser());
    }

    @Override
    public boolean canEdit(ClientScope clientScope) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENT_SCOPES);
        return clientScope.getClient().isActive() && clientScope.getClient().getApplication().isActive() &&
                clientScope.getClient().getApplication().getOwner().idMatch(getUser());
    }

    @Override
    public boolean canDelete(ClientScope clientScope) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> list, ClientScope clientScope) {
    }

    @Override
    public void beforeCreate(ClientScope clientScope) {
        if (clientScope.getReason() != null && clientScope.getReason().trim().isEmpty()) {
            clientScope.setReason(null);
        }
    }

    @Override
    public void beforeEdit(ClientScope clientScope, ClientScope newScope) {
        beforeCreate(newScope);
    }

    @QueryParam("clientId")
    UUID clientId;

    @QueryParam("scopeId")
    UUID scopeId;

    @QueryParam("active")
    Boolean active;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<ClientScope> root) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENT_SCOPES);

        list.add(cb.equal(root.join(ClientScope_.scope)
                .join(Scope_.application).get(Application_.owner), getUser()));

        if (clientId != null) {
            list.add(cb.equal(root.join(ClientScope_.client).get(Client_.id), clientId));
        }

        if (scopeId != null) {
            list.add(cb.equal(root.join(ClientScope_.scope).get(Scope_.id), scopeId));
        }
    }

    @Override
    public void afterCreate(ClientScope clientScope) {

    }

    @Override
    public void beforeSend(ClientScope clientScope) {

    }
}
