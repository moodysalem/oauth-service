package com.oauth2cloud.server.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.Scope;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterClientRequest {
    private PublicApplication publicApplication;
    private List<PublicScope> publicScopes;
    private Client client;

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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
