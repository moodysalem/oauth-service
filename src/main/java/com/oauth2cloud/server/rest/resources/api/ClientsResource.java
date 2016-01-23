package com.oauth2cloud.server.rest.resources.api;

import com.moodysalem.util.RandomStringUtil;
import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.TokenFeature;
import com.oauth2cloud.server.rest.resources.BaseEntityResource;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@TokenFeature.ReadToken
@Path(OAuth2Application.API + "/clients")
public class ClientsResource extends BaseEntityResource<Client> {
    public static final String MANAGE_CLIENTS = "manage_clients";

    @Override
    public Class<Client> getEntityClass() {
        return Client.class;
    }


    @Override
    public boolean canCreate(Client client) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENTS);

        if (client.getApplication() == null) {
            return false;
        }

        com.oauth2cloud.server.hibernate.model.Application ap = em.find(com.oauth2cloud.server.hibernate.model.Application.class, client.getApplication().getId());
        return ap != null && ap.getOwner().idMatch(getUser());
    }

    @Override
    public boolean canEdit(Client client) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENTS);
        return client.getApplication().getOwner().idMatch(getUser());
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
        client.setCreator(getUser());
        client.setIdentifier(RandomStringUtil.randomAlphaNumeric(64));
        client.setSecret(RandomStringUtil.randomAlphaNumeric(64));
    }

    @Override
    public void beforeEdit(Client client, Client t1) {
    }


    @QueryParam("search")
    String search;

    @QueryParam("applicationId")
    Long applicationId;

    @QueryParam("active")
    Boolean active;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Client> root) {
        mustBeLoggedIn();
        checkScope(MANAGE_CLIENTS);

        list.add(cb.equal(root.join("application").get("owner"), getUser()));
        list.add(cb.equal(root.get("active"), true));
        list.add(cb.equal(root.join("application").get("active"), true));

        if (applicationId != null) {
            list.add(cb.equal(root.join("application").get("id"), applicationId));
        }

        if (search != null) {
            Predicate toAdd = null;
            for (String s : search.split(" ")) {
                if (s.trim().length() > 0) {
                    Predicate sp = cb.like(cb.upper(root.get("name")), "%" + s.trim().toUpperCase() + "%");
                    toAdd = toAdd == null ? sp : cb.and(sp, toAdd);
                }
            }
            if (toAdd != null) {
                list.add(toAdd);
            }
        }

        if (active != null) {
            list.add(cb.equal(root.get("active"), active));
        }
    }

    @Override
    public void afterCreate(Client client) {

    }

    @Override
    public void beforeSend(Client client) {

    }
}
