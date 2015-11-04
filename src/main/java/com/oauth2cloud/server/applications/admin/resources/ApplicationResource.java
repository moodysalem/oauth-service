package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.Application;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import java.util.List;

@Path("applications")
public class ApplicationResource extends BaseEntityResource<Application> {
    @Override
    public Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    public boolean canCreate(Application application) {
        mustBeLoggedIn();
        return true;
    }

    @Override
    public boolean canEdit(Application application) {
        mustBeLoggedIn();
        return application.getOwner().equals(getUser());
    }

    @Override
    public boolean canDelete(Application application) {
        mustBeLoggedIn();
        return application.getOwner().equals(getUser());
    }

    @Override
    protected void validateEntity(List<String> list, Application application) {

    }

    @Override
    public void beforeCreate(Application application) {
    }

    @Override
    public void beforeEdit(Application application, Application t1) {

    }

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Application> root) {
        list.add(cb.equal(root.get("owner"), getUser()));
    }

    @Override
    public void afterCreate(Application application) {

    }

    @Override
    public void beforeSend(Application application) {

    }
}
