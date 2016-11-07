package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.api.LoginErrorCode;
import com.oauth2cloud.server.model.db.Client;

/**
 * This is the model that gets passed to Authorize.ftl
 */
public class SentEmailModel implements HeadProperties {
    private final Client client;

    public SentEmailModel(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
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
