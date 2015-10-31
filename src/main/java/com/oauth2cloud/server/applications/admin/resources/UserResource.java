package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.User;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import java.util.List;

@Path("users")
public class UserResource extends BaseEntityResource<User> {
    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public boolean canCreate(User user) {
        return false;
    }

    @Override
    public boolean canEdit(User user) {
        return false;
    }

    @Override
    public boolean canDelete(User user) {
        return false;
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

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<User> root) {
        list.add(cb.equal(root.join("application").get("owner"), getUser()));
    }

    @Override
    public void afterCreate(User user) {

    }

    @Override
    public void beforeSend(User user) {

    }
}
