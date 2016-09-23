package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.UserCode;

public class UserCodeEmailModel implements HeadProperties {
    private final UserCode userCode;
    private final String url;

    public UserCodeEmailModel(UserCode userCode, String url) {
        this.userCode = userCode;
        this.url = url;
    }

    public UserCode getUserCode() {
        return userCode;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getStylesheetUrl() {
        return userCode != null ? userCode.getUser().getApplication().getStylesheetUrl() : null;
    }

    @Override
    public String getFaviconUrl() {
        return userCode != null ? userCode.getUser().getApplication().getFaviconUrl() : null;
    }
}
