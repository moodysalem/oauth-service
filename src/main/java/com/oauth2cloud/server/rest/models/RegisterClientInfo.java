package com.oauth2cloud.server.rest.models;

import java.util.List;

public class RegisterClientInfo {
    private PublicApplication publicApplication;
    private List<PublicScope> publicScopes;

    public PublicApplication getPublicApplication() {
        return publicApplication;
    }

    public void setPublicApplication(PublicApplication publicApplication) {
        this.publicApplication = publicApplication;
    }

    public List<PublicScope> getPublicScopes() {
        return publicScopes;
    }

    public void setPublicScopes(List<PublicScope> publicScopes) {
        this.publicScopes = publicScopes;
    }
}
