package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.Application;

import java.util.UUID;

public class PublicApplication {
    private UUID id;
    private String name;
    private String supportEmail;
    private String description;
    private String logoUrl;


    public PublicApplication(Application application) {
        if (application == null) {
            throw new NullPointerException();
        }
        setId(application.getId());
        setDescription(application.getDescription());
        setLogoUrl(application.getLogoUrl());
        setName(application.getName());
        setSupportEmail(application.getSupportEmail());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
