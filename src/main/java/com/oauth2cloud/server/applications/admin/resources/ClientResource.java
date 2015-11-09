package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.Client;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import java.util.List;

@Path("clients")
public class ClientResource extends BaseEntityResource<Client>{

    @Override
    public Class<Client> getEntityClass() {
        return Client.class;
    }



    @Override
    public boolean canCreate(Client client) {
        return false;
    }

    @Override
    public boolean canEdit(Client client) {
        return false;
    }

    @Override
    public boolean canDelete(Client client) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> list, Client client) {

    }

    @Override
    public void beforeCreate(Client client) {

    }

    @Override
    public void beforeEdit(Client client, Client t1) {

    }

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Client> root) {
        list.add(cb.equal(root.join("application").get("owner"), getUser()));
    }

    @Override
    public void afterCreate(Client client) {

    }

    @Override
    public void beforeSend(Client client) {

    }
}
