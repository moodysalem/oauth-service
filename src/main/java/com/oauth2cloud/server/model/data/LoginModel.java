package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.api.LoginErrorCode;
import com.oauth2cloud.server.model.db.Client;

/**
 * This is the model that gets passed to Authorize.ftl
 */
public class LoginModel implements HeadProperties {
    private final boolean sentEmail;
    private final LoginErrorCode loginErrorCode;
    private final Client client;

    public LoginModel(Client client, LoginErrorCode loginErrorCode, boolean sentEmail) {
        this.sentEmail = sentEmail;
        this.loginErrorCode = loginErrorCode;
        this.client = client;
    }

    public boolean isSentEmail() {
        return sentEmail;
    }

    public LoginErrorCode getLoginErrorCode() {
        return loginErrorCode;
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
