package com.oauth2cloud.server.rest.models;

import java.util.List;

public class RegisterClientInfo {
    private PublicApplication application;
    private List<PublicScope> scopes;

    public PublicApplication getApplication() {
        return application;
    }

    public void setApplication(PublicApplication application) {
        this.application = application;
    }

    public List<PublicScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<PublicScope> scopes) {
        this.scopes = scopes;
    }
}
