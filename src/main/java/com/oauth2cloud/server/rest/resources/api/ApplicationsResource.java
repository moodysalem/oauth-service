package com.oauth2cloud.server.rest.resources.api;

import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.TokenFeature;
import com.oauth2cloud.server.rest.resources.BaseEntityResource;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@TokenFeature.ReadToken
@Path(OAuth2Application.API + "/applications")
public class ApplicationsResource extends BaseEntityResource<com.oauth2cloud.server.hibernate.model.Application> {

    public static final String MANAGE_APPLICATIONS = "manage_applications";

    @Override
    public Class<com.oauth2cloud.server.hibernate.model.Application> getEntityClass() {
        return com.oauth2cloud.server.hibernate.model.Application.class;
    }

    @Override
    public boolean canCreate(com.oauth2cloud.server.hibernate.model.Application application) {
        mustBeLoggedIn();
        checkScope(MANAGE_APPLICATIONS);
        return true;
    }

    @Override
    public boolean canEdit(com.oauth2cloud.server.hibernate.model.Application application) {
        mustBeLoggedIn();
        checkScope(MANAGE_APPLICATIONS);
        return application.getOwner().idMatch(getUser());
    }

    @Override
    public boolean canDelete(com.oauth2cloud.server.hibernate.model.Application application) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> list, com.oauth2cloud.server.hibernate.model.Application application) {

        if (application.getFacebookAppId() != null && application.getFacebookAppSecret() == null ||
            application.getFacebookAppId() == null && application.getFacebookAppSecret() != null) {
            list.add("Both Facebook application ID and Facebook application secret must be specified together.");
        }

        if (application.getAmazonClientId() != null && application.getAmazonClientSecret() == null ||
            application.getAmazonClientId() == null && application.getAmazonClientSecret() != null) {
            list.add("Both Amazon application ID and Amazon application secret must be specified together.");
        }

        if (application.getGoogleClientId() != null && application.getGoogleClientSecret() == null ||
            application.getGoogleClientId() == null && application.getGoogleClientSecret() != null) {
            list.add("Both Google application ID and Google application secret must be specified together.");
        }

        if (application.getFacebookAppId() != null && application.getFacebookAppSecret() != null) {
            FacebookClient fbc = new DefaultFacebookClient(Version.VERSION_2_5);
            try {
                FacebookClient.AccessToken at = fbc.obtainAppAccessToken(application.getFacebookAppId().toString(), application.getFacebookAppSecret());
                if (at.getAccessToken() == null) {
                    list.add("Invalid Facebook application ID or secret.");
                }
            } catch (Exception e) {
                list.add("Invalid Facebook application ID or secret: " + e.getMessage());
            }
        }

    }

    @Override
    public void beforeCreate(com.oauth2cloud.server.hibernate.model.Application application) {
        application.setOwner(getUser());

        setNullsForEmptyStrings(application);
    }

    @Override
    public void beforeEdit(com.oauth2cloud.server.hibernate.model.Application application, com.oauth2cloud.server.hibernate.model.Application t1) {
        setNullsForEmptyStrings(t1);
    }

    @QueryParam("active")
    Boolean active;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<com.oauth2cloud.server.hibernate.model.Application> root) {
        checkScope(MANAGE_APPLICATIONS);
        list.add(cb.equal(root.get("owner"), getUser()));

        if (active != null) {
            list.add(cb.equal(root.get("active"), active));
        }
    }

    @Override
    public void afterCreate(com.oauth2cloud.server.hibernate.model.Application application) {

    }

    @Override
    public void beforeSend(com.oauth2cloud.server.hibernate.model.Application application) {

    }

    private void setNullsForEmptyStrings(com.oauth2cloud.server.hibernate.model.Application application) {
        if (application.getLegacyUrl() != null && application.getLegacyUrl().trim().isEmpty()) {
            application.setLegacyUrl(null);
        }
        if (application.getStylesheetUrl() != null && application.getStylesheetUrl().trim().isEmpty()) {
            application.setLegacyUrl(null);
        }
        if (application.getFaviconUrl() != null && application.getFaviconUrl().trim().isEmpty()) {
            application.setFaviconUrl(null);
        }
        if (application.getLogoUrl() != null && application.getLogoUrl().trim().isEmpty()) {
            application.setLogoUrl(null);
        }

        if (application.getDescription() != null && application.getDescription().trim().isEmpty()) {
            application.setDescription(null);
        }

        if (application.getFacebookAppSecret() != null && application.getFacebookAppSecret().isEmpty()) {
            application.setFacebookAppSecret(null);
        }

        if (application.getGoogleClientId() != null && application.getGoogleClientId().isEmpty()) {
            application.setGoogleClientId(null);
        }

        if (application.getGoogleClientSecret() != null && application.getGoogleClientSecret().isEmpty()) {
            application.setGoogleClientSecret(null);
        }

        if (application.getAmazonClientId() != null && application.getAmazonClientId().isEmpty()) {
            application.setAmazonClientId(null);
        }

        if (application.getAmazonClientSecret() != null && application.getAmazonClientSecret().isEmpty()) {
            application.setAmazonClientSecret(null);
        }
    }
}
