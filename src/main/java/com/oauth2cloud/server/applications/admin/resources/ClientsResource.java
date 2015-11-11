package com.oauth2cloud.server.applications.admin.resources;

import com.leaguekit.util.RandomStringUtil;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Client;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import java.util.List;

@Path("clients")
public class ClientsResource extends BaseEntityResource<Client> {

    @Override
    public Class<Client> getEntityClass() {
        return Client.class;
    }


    @Override
    public boolean canCreate(Client client) {
        if (client.getApplication() == null) {
            return false;
        }
        Application ap = em.find(Application.class, client.getApplication().getId());
        if (ap == null) {
            return false;
        }
        if (!ap.getOwner().equals(getUser())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canEdit(Client client) {
        return client.getApplication().getOwner().equals(getUser());
    }

    @Override
    public boolean canDelete(Client client) {
        return canEdit(client);
    }

    @Override
    protected void validateEntity(List<String> list, Client client) {

    }

    @Override
    public void beforeCreate(Client client) {
        client.setIdentifier(RandomStringUtil.randomAlphaNumeric(64));
        client.setSecret(RandomStringUtil.randomAlphaNumeric(64));
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
