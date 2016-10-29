package com.oauth2cloud.server.rest.endpoints.api;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.config.EntityResourceConfig;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.Application_;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Api("crud")
@Path("applications")
@TokenFilter.ReadToken
public class ApplicationsResource extends VersionedEntityResource<Application> {
    @Override
    public Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    public boolean canMerge(Application oldData, Application newData) {
        final User requestingUser = getUser();
        return (oldData == null || oldData.getOwner().idMatch(requestingUser));
    }

    @Override
    public void beforeMerge(Application oldData, Application newData) {
        newData.setOwner(getUser());
    }

    @Override
    public void afterMerge(Application entity) {

    }

    @Override
    public boolean canDelete(Application application) {
        return application.getOwner().idMatch(getUser());
    }

    @Override
    public void getPredicatesFromRequest(List<Predicate> predicates, Root<Application> root) {
        predicates.add(cb.equal(root.get(Application_.owner), getUser()));
    }

    @Override
    public void beforeSend(List<Application> entity) {

    }

    @Override
    public void checkAccess(Action action) {
        requireScope("manage_applications");
    }

}
