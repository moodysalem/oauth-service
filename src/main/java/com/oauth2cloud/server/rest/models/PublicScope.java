package com.oauth2cloud.server.rest.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth2cloud.server.hibernate.model.Scope;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@JsonIdentityInfo(
        generator = JSOGGenerator.class
)
public class PublicScope {
    private long id;
    private String name;
    private String thumbnail;
    private String displayName;
    private boolean requiresApprovalFromApplication;

    public PublicScope(Scope scope) {
        if (scope != null) {
            throw new NullPointerException();
        }
        setId(scope.getId());
        setName(scope.getName());
        setThumbnail(scope.getThumbnail());
        setDisplayName(scope.getDisplayName());
        setRequiresApprovalFromApplication(scope.isRequiresApprovalFromApplication());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isRequiresApprovalFromApplication() {
        return requiresApprovalFromApplication;
    }

    public void setRequiresApprovalFromApplication(boolean requiresApprovalFromApplication) {
        this.requiresApprovalFromApplication = requiresApprovalFromApplication;
    }
}
