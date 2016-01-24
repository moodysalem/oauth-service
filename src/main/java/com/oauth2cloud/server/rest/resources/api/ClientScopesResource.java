package com.oauth2cloud.server.rest.resources.api;

import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.ClientScope;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.TokenFeature;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@TokenFeature.ReadToken
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
        if (clientScope.getClient() != null && clientScope.getClient().getId() > 0) {
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
    Long clientId;

    @QueryParam("scopeId")
    Long scopeId;

    @QueryParam("active")
    Boolean active;

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

        if (active != null) {
            list.add(cb.equal(root.get("active"), active));
        }
    }

    @Override
    public void afterCreate(ClientScope clientScope) {

    }

    @Override
    public void beforeSend(ClientScope clientScope) {

    }
}
