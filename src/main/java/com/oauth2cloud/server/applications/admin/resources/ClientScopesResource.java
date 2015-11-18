package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.ClientScope;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("clientscopes")
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
        if (clientScope.getClient() != null && clientScope.getClient().getId() > 0) {
            c = em.find(Client.class, clientScope.getClient().getId());
        }
        return c != null && c.getApplication().getOwner().equals(getUser());
    }

    @Override
    public boolean canEdit(ClientScope clientScope) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENT_SCOPES);
        return clientScope.getClient().getApplication().getOwner().equals(getUser());
    }

    @Override
    public boolean canDelete(ClientScope clientScope) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENT_SCOPES);
        return canEdit(clientScope);
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
    Long clientId;

    @QueryParam("scopeId")
    Long scopeId;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<ClientScope> root) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENT_SCOPES);

        list.add(cb.equal(root.join("scope").join("application").get("owner"), getUser()));

        if (clientId != null) {
            list.add(cb.equal(root.join("client").get("id"), clientId));
        }

        if (scopeId != null) {
            list.add(cb.equal(root.join("scope").get("id"), scopeId));
        }
    }

    @Override
    public void afterCreate(ClientScope clientScope) {

    }

    @Override
    public void beforeSend(ClientScope clientScope) {

    }
}
