package com.oauth2cloud.server.resources.response.models;

import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.ClientScope;
import com.oauth2cloud.server.hibernate.model.Token;

import java.util.List;

public class PermissionsModel {
    private Client client;
    private Token token;
    private List<ClientScope> clientScopes;
    private boolean rememberMe;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(List<ClientScope> clientScopes) {
        this.clientScopes = clientScopes;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
