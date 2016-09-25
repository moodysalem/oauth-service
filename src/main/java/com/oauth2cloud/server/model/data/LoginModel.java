package com.oauth2cloud.server.model.data;

/**
 * This is the model that gets passed to Authorize.ftl
 */
public class LoginModel extends AuthorizeModel {
    private boolean sentEmail;
    private String loginError;

    public boolean isSentEmail() {
        return sentEmail;
    }

    public void setSentEmail(boolean sentEmail) {
        this.sentEmail = sentEmail;
    }

    public String getLoginError() {
        return loginError;
    }

    public void setLoginError(String loginError) {
        this.loginError = loginError;
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
