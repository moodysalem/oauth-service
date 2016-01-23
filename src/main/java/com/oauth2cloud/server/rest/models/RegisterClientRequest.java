package com.oauth2cloud.server.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.ClientScope;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterClientRequest {
    private Client client;
    private List<ClientScope> clientScopes;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(List<ClientScope> clientScopes) {
        this.clientScopes = clientScopes;
    }
}
