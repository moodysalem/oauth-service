package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.Scope;

import java.util.List;

public class RegisterClientRequest {
    private Application application;
    private List<Scope> scopes;
    private Client client;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
