package com.oauth2cloud.server.applications.admin.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth2cloud.server.hibernate.model.Application;

public class PublicApplication {
    public PublicApplication(Application application) {
        if (application == null) {
            throw new NullPointerException();
        }
        this.application = application;
    }

    @JsonIgnore
    private Application application;

    public Application getApplication() {
        return application;
    }

    public long getId() {
        return application.getId();
    }

    public String getName() {
        return application.getName();
    }

    public String getSupportEmail() {
        return application.getSupportEmail();
    }

    public String getDescription() {
        return application.getDescription();
    }

}
