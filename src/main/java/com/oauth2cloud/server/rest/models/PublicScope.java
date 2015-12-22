package com.oauth2cloud.server.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth2cloud.server.hibernate.model.Scope;

public class PublicScope {
    @JsonIgnore
    private Scope scope;

    public PublicScope(Scope scope) {
        this.scope = scope;
    }

    public String getName() {
        return scope.getName();
    }

    public PublicApplication getApplication() {
        return new PublicApplication(scope.getApplication());
    }

    public String getDescription() {
        return scope.getDescription();
    }

    public String getThumbnail() {
        return scope.getThumbnail();
    }

    public String getDisplayName() {
        return scope.getDisplayName();
    }

    public boolean isRequiresApprovalFromApplication() {
        return scope.isRequiresApprovalFromApplication();
    }

}
