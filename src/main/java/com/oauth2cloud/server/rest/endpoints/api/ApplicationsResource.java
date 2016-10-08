package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.Application_;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.rest.endpoints.api.base.VersionedEntityResource;
import com.oauth2cloud.server.rest.filter.TokenFilter;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import java.util.List;

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
        return (oldData == null || oldData.getOwner().idMatch(requestingUser)) &&
                (newData.getOwner().idMatch(requestingUser));
    }

    @Override
    public void beforeMerge(Application oldData, Application newData) {

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
        predicates.add(cb.equal(root.get(Application_.owner), TokenFilter.getUser(request)));
    }

    @Override
    public void beforeSend(List<Application> entity) {

    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

}
