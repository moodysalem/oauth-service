package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.Client;

public class ClientErrorModel implements HeadProperties {
    private final Client client;
    private final String error;

    public ClientErrorModel(Client client, String error) {
        this.error = error;
        this.client = client;
    }

    public String getError() {
        return error;
    }

    @Override
    public String getStylesheetUrl() {
        return client != null ? client.getApplication().getStylesheetUrl() : null;
    }

    @Override
    public String getFaviconUrl() {
        return client != null ? client.getApplication().getFaviconUrl() : null;
    }
}
