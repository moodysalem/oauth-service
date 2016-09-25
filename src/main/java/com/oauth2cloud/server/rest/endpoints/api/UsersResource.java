package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.Application_;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.model.db.User_;
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
@Path(OAuth2Application.API_PATH + "/users")
public class UsersResource extends VersionedEntityResource<User> {
    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public boolean canMerge(User oldData, User newData) {
        if (newData.getApplication() == null) {
            return false;
        }

        final Application ap = em.find(Application.class, newData.getApplication().getId());

        return ap != null && ap.getOwner().idMatch(getUser());
    }

    @Override
    public void beforeMerge(User oldData, User newData) {

    }

    @Override
    public void afterMerge(User entity) {

    }

    @Override
    public boolean canDelete(User user) {
        return false;
    }

    @QueryParam("applicationId")
    private Set<UUID> applicationId;

    @Override
    public void getPredicatesFromRequest(List<Predicate> list, Root<User> root) {
        list.add(cb.equal(root.join(User_.application).get(Application_.owner), getUser()));

        if (applicationId != null && !applicationId.isEmpty()) {
            list.add(root.join(User_.application).get(Application_.id).in(applicationId));
        }
    }

    @Override
    public void beforeSend(List<User> entity) {

    }

    @Override
    public boolean requiresLogin() {
        return true;
    }
}
