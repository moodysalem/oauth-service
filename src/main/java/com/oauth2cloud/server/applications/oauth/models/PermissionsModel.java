package com.oauth2cloud.server.applications.oauth.models;

import com.oauth2cloud.server.hibernate.model.ClientScope;
import com.oauth2cloud.server.hibernate.model.Token;

import java.util.List;

/**
 * This is the model that gets passed to Permissions.ftl
 */
public class PermissionsModel extends AuthorizeModel {
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

    @Override
    public String getStylesheetUrl() {
        return getClient().getApplication().getStylesheetUrl();
    }
}
