package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.ClientScope;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class ClientScopesResource extends BaseEntityResource<ClientScope> {
    @Override
    public Class<ClientScope> getEntityClass() {
        return ClientScope.class;
    }

    @Override
    public boolean canCreate(ClientScope clientScope) {
        return false;
    }

    @Override
    public boolean canEdit(ClientScope clientScope) {
        return false;
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

    }

    @Override
    public void beforeEdit(ClientScope clientScope, ClientScope t1) {

    }

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<ClientScope> root) {
        list.add(cb.equal(root.join("scope").join("application").get("owner"), getUser()));
    }

    @Override
    public void afterCreate(ClientScope clientScope) {

    }

    @Override
    public void beforeSend(ClientScope clientScope) {

    }
}
