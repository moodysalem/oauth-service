package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.LoginCode;

import java.util.Set;

/**
 * This is the model that gets passed to Permissions.ftl
 */
public class PermissionsModel implements HeadProperties {
    public PermissionsModel(final LoginCode loginCode, final Set<UserClientScope> userClientScopes) {
        this.loginCode = loginCode;
        this.userClientScopes = userClientScopes;
    }

    private final LoginCode loginCode;
    private final Set<UserClientScope> userClientScopes;

    public Set<UserClientScope> getUserClientScopes() {
        return userClientScopes;
    }

    public LoginCode getLoginCode() {
        return loginCode;
    }

    @Override
    public String getStylesheetUrl() {
        return getLoginCode().getClient().getApplication().getStylesheetUrl();
    }

    @Override
    public String getFaviconUrl() {
        return getLoginCode().getClient().getApplication().getFaviconUrl();
    }

    public boolean isAlreadyAuthorized() {
        return userClientScopes == null || userClientScopes.isEmpty();
    }
}
