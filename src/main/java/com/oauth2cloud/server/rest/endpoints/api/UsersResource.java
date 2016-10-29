package com.oauth2cloud.server.rest.endpoints.api;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Api("crud")
@Path("users")
@TokenFilter.ReadToken
public class UsersResource extends VersionedEntityResource<User> {
    @ApiOperation(
            value = "Create User Group",
            notes = "Creates a user group used for linking multiple users together"
    )
    @POST
    @Path("create-group")
    public Response createGroup(
            @ApiParam(value = "The application ID of the user group", required = true)
            final UUID applicationId
    ) {
        requireLoggedIn();

        if (applicationId == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "applicationId is required in the request body");
        }

        final Application application = em.find(Application.class, applicationId);

        if (application == null || !application.getOwner().idMatch(getUser())) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid application ID");
        }

        final UserGroup saving = new UserGroup();
        saving.setApplication(application);

        final UserGroup saved;
        try {
            saved = TXHelper.withinTransaction(em, () -> em.merge(saving));
        } catch (Exception e) {
            throw RequestProcessingException.from(e);
        }

        em.refresh(saved);

        return Response.ok(saved).build();
    }

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
        if (newData.getGroup() != null) {
            if (newData.getGroup().getId() == null) {
                return false;
            }

            final UserGroup ug = em.find(UserGroup.class, newData.getGroup().getId());
            if (ug == null || !ug.getApplication().idMatch(userApplication)) {
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