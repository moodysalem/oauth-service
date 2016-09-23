package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.UserCode;

public class ResetPasswordModel implements HeadProperties {
    private final Application application;
    private final String error;
    private final UserCode userCode;
    private final boolean success;
    private final String referrer;

    public ResetPasswordModel(Application application, String error, UserCode userCode, boolean success, String referrer) {
        this.application = application;
        this.error = error;
        this.userCode = userCode;
        this.success = success;
        this.referrer = referrer;
    }

    public String getError() {
        return error;
    }

    public Application getApplication() {
        return application;
    }

    public boolean isSuccess() {
        return success;
    }

    public UserCode getUserCode() {
        return userCode;
    }

    public String getReferrer() {
        return referrer;
    }

    @Override
    public String getStylesheetUrl() {
        return userCode != null ? userCode.getUser().getApplication().getStylesheetUrl() :
                (application != null ? application.getStylesheetUrl() : null);
    }

    @Override
    public String getFaviconUrl() {
        return userCode != null ? userCode.getUser().getApplication().getFaviconUrl() :
                (application != null ? application.getFaviconUrl() : null);
    }
}
