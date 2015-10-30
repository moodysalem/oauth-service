package com.oauth2cloud.server.resources.admin.resources;

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
        return false;
    }

    @Override
    public boolean canEdit(Application application) {
        return false;
    }

    @Override
    public boolean canDelete(Application application) {
        return false;
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
        list.add(cb.equal(root.get("ownerId"), 0));
    }

    @Override
    public void afterCreate(Application application) {

    }

    @Override
    public void beforeSend(Application application) {

    }
}
