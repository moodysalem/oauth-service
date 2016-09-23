package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.model.Application_;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.AuthorizationHeaderTokenFeature;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@AuthorizationHeaderTokenFeature.ReadToken
@Path(OAuth2Application.API_PATH + "/applications")
public class ApplicationsResource extends BaseEntityResource<Application> {

    public static final String MANAGE_APPLICATIONS = "manage_applications";

    @Override
    public Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    public boolean canCreate(Application application) {
        mustBeLoggedIn();
        checkScope(MANAGE_APPLICATIONS);
        return true;
    }

    @Override
    public boolean canEdit(Application application) {
        mustBeLoggedIn();
        checkScope(MANAGE_APPLICATIONS);
        return application.getOwner().idMatch(getUser());
    }

    @Override
    public boolean canDelete(Application application) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> list, Application application) {
        if (application.getGoogleClientId() != null && application.getGoogleClientSecret() == null ||
                application.getGoogleClientId() == null && application.getGoogleClientSecret() != null) {
            list.add("Both Google application ID and Google application secret must be specified together.");
        }
    }

    @Override
    public void beforeCreate(Application application) {
        application.setOwner(getUser());
    }

    @QueryParam("active")
    private Boolean active;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Application> root) {
        checkScope(MANAGE_APPLICATIONS);
        list.add(cb.equal(root.get(Application_.owner), getUser()));

        if (active != null) {
            list.add(cb.equal(root.get(Application_.active), active));
        }
    }

}
