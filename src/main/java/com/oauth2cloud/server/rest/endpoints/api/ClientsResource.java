package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.Application_;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.Client_;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@TokenFilter.ReadToken
@Path(OAuth2Application.API_PATH + "/clients")
public class ClientsResource extends VersionedEntityResource<Client> {
    @Override
    public Class<Client> getEntityClass() {
        return Client.class;
    }

    @Override
    public boolean canMerge(Client oldData, Client newData) {
        if (newData.getApplication() == null) {
            return false;
        }

        final Application application = em.find(Application.class, newData.getApplication().getId());
        return application != null &&
                application.getOwner().idMatch(getUser());
    }

    @Override
    public void beforeMerge(Client oldData, Client newData) {

    }

    @Override
    public void afterMerge(Client entity) {

    }


    @Override
    public boolean canDelete(Client client) {
        return client.getApplication().getOwner().idMatch(getUser());
    }

    @QueryParam("applicationId")
    private Set<UUID> applicationId;

    @Override
    public void getPredicatesFromRequest(List<Predicate> list, Root<Client> root) {
        list.add(cb.equal(root.join(Client_.application).get(Application_.owner), getUser()));

        if (applicationId != null && !applicationId.isEmpty()) {
            list.add(root.join(Client_.application).get(Application_.id).in(applicationId));
        }
    }

    @Override
    public void beforeSend(List<Client> entity) {

    }

    @Override
    public boolean requiresLogin() {
        return true;
    }
}
