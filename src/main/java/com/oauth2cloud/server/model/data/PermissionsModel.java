package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.ClientScope;
import com.oauth2cloud.server.model.db.Token;

import java.util.Set;

/**
 * This is the model that gets passed to Permissions.ftl
 */
public class PermissionsModel extends AuthorizeModel {
    public PermissionsModel(Token token, Set<ClientScope> clientScopes, boolean rememberMe) {
        this.token = token;
        this.clientScopes = clientScopes;
        this.rememberMe = rememberMe;
    }

    private final Token token;
    private final Set<ClientScope> clientScopes;
    private final boolean rememberMe;

    public Set<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public Token getToken() {
        return token;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    @Override
    public String getStylesheetUrl() {
        return getClient().getApplication().getStylesheetUrl();
    }

    @Override
    public String getFaviconUrl() {
        return getClient().getApplication().getFaviconUrl();
    }
}
