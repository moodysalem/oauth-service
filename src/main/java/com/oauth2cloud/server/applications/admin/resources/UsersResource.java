package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.User;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("users")
public class UsersResource extends BaseEntityResource<User> {
    public static final String MANAGE_USERS = "manage_users";

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public boolean canCreate(User user) {
        mustBeLoggedIn();
        checkScope(MANAGE_USERS);
        return false;
    }

    @Override
    public boolean canEdit(User user) {
        mustBeLoggedIn();
        checkScope(MANAGE_USERS);
        return false;
    }

    @Override
    public boolean canDelete(User user) {
        return canEdit(user);
    }

    @Override
    protected void validateEntity(List<String> list, User user) {

    }

    @Override
    public void beforeCreate(User user) {

    }

    @Override
    public void beforeEdit(User user, User t1) {

    }

    @QueryParam("search")
    String search;

    @QueryParam("applicationId")
    Long appliationId;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<User> root) {
        mustBeLoggedIn();
        checkScope(MANAGE_USERS);

        list.add(cb.equal(root.join("application").get("owner"), getUser()));

        if (search != null) {
            list.add(cb.like(root.get("firstName"), "%" + search + "%"));
        }
    }

    @Override
    public void afterCreate(User user) {

    }

    @Override
    public void beforeSend(User user) {

    }
}
