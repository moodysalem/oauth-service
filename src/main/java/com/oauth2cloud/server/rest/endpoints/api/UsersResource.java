package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import io.swagger.annotations.Api;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Api("crud")
@Path("users")
@TokenFilter.ReadToken
public class UsersResource extends VersionedEntityResource<User> {
    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public boolean canMerge(User oldData, User newData) {
        final Application userApplication;

        if (oldData == null) {
            if (newData.getApplication() == null || newData.getApplication().getId() == null) {
                return false;
            } else {
                userApplication = em.find(Application.class, newData.getApplication().getId());
            }
        } else {
            if (!oldData.getApplication().getOwner().idMatch(getUser())) {
                return false;
            }

            userApplication = oldData.getApplication();
        }


        if (userApplication == null || !userApplication.getOwner().idMatch(getUser())) {
            return false;
        }

        // all users in the group must be associated with the same application
        if (newData.getGroup() != null && newData.getGroup().getId() != null) {
            final UserGroup ug = em.find(UserGroup.class, newData.getGroup().getId());
            if (ug.getUsers().stream().anyMatch(user -> !userApplication.idMatch(user.getApplication()))) {
                return false;
            }
        }

        return true;
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
    public void checkAccess(Action action) {
        requireScope("manage_users");
    }
}