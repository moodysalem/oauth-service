package com.oauth2cloud.server.rest.resources.api;

import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.TokenFeature;
import com.oauth2cloud.server.rest.models.PublicApplication;
import com.oauth2cloud.server.rest.resources.BaseEntityResource;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@TokenFeature.ReadToken
@Path(OAuth2Application.API + "/publicapplications")
public class PublicApplicationsResource extends BaseEntityResource<Application> {

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
    public Response getList() {
        Response r = super.getList();

        if (r.getStatus() == 200) {
            return Response.fromResponse(r).entity(
                ((List<Application>) (r.getEntity())).stream().map(PublicApplication::new).collect(Collectors.toList())
            ).build();
        }

        return r;
    }

    @Override
    public Response get(long id) {
        Response r = super.get(id);

        if (r.getStatus() == 200) {
            return Response.fromResponse(r).entity(
                new PublicApplication((Application) r.getEntity())
            ).build();
        }

        return r;
    }

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Application> root) {

    }

    @Override
    public void afterCreate(Application application) {

    }

    @Override
    public void beforeSend(Application application) {

    }
}
