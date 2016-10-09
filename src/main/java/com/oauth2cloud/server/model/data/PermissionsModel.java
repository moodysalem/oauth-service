package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.ClientScope;
import com.oauth2cloud.server.model.db.Token;

import java.util.Set;

/**
 * This is the model that gets passed to Permissions.ftl
 */
public class PermissionsModel implements HeadProperties {
    public PermissionsModel(final Client client, final Token token, final Set<ClientScope> clientScopes) {
        this.client = client;
        this.token = token;
        this.clientScopes = clientScopes;
    }

    private final Token token;
    private final Set<ClientScope> clientScopes;
    private final Client client;

    public Client getClient() {
        return client;
    }

    public Set<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public Token getToken() {
        return token;
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
