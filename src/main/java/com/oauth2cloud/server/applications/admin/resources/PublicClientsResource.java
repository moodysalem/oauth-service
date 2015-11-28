package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.applications.admin.models.PublicClient;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Client;

import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gets the applications but wraps them all in PublicApplication instances
 */
@Path("publicapplications")
public class PublicClientsResource extends BaseEntityResource<Client> {

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public boolean canCreate(Client entity) {
        return false;
    }

    @Override
    public boolean canEdit(Client entity) {
        return false;
    }

    @Override
    public boolean canDelete(Client entity) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> errors, Client entity) {

    }

    @Override
    public void beforeCreate(Client entity) {

    }

    @Override
    public void beforeEdit(Client oldEntity, Client entity) {

    }

    @Override
    protected void getPredicatesFromRequest(List<Predicate> predicates, Root<Client> root) {
        predicates.add(cb.equal(root.join("application").get("publicClientRegistration"), true));
    }

    @Override
    public void afterCreate(Client entity) {

    }

    @Override
    public void beforeSend(Client entity) {

    }

    @Override
    public Response getList() {
        Response r = super.getList();
        if (!r.hasEntity() || !(r.getEntity() instanceof List)) {
            return r;
        }
        return Response.fromResponse(r)
            // replace the entity in the response with public application instances
            .entity(((List<Client>) r.getEntity()).stream().map(PublicClient::new).collect(Collectors.toList()))
            .build();
    }

    @Override
    public Class<Client> getEntityClass() {
        return Client.class;
    }

    @Override
    public Response get(long id) {
        Response r = super.get(id);
        if (!r.hasEntity() || !(r.getEntity() instanceof Application)) {
            return r;
        }
        return Response.fromResponse(r).entity(new PublicClient((Client) r.getEntity())).build();
    }

}
