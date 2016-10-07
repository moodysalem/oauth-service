package com.oauth2cloud.server.model.data;

/**
 * This is the model that gets passed to Authorize.ftl
 */
public class LoginModel extends AuthorizeModel {
    private boolean sentEmail;

    public boolean isSentEmail() {
        return sentEmail;
    }

    public void setSentEmail(boolean sentEmail) {
        this.sentEmail = sentEmail;
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
