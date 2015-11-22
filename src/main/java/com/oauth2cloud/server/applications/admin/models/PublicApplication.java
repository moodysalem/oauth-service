package com.oauth2cloud.server.applications.admin.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth2cloud.server.hibernate.model.Application;

public class PublicApplication {
    public PublicApplication(Application application) {
        if (application == null) {
            throw new NullPointerException();
        }
        setApplication(application);
    }

    @JsonIgnore
    private Application application;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getName() {
        return application.getName();
    }

    public String getSupportEmail() {
        return application.getSupportEmail();
    }

}
