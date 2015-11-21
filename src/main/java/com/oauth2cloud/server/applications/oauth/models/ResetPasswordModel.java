package com.oauth2cloud.server.applications.oauth.models;

import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.UserCode;

public class ResetPasswordModel implements HeadProperties {
    private Application application;
    private String error;
    private UserCode userCode;
    private boolean success;
    private String referer;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UserCode getUserCode() {
        return userCode;
    }

    public void setUserCode(UserCode userCode) {
        this.userCode = userCode;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Override
    public String getStylesheetUrl() {
        return userCode != null ? userCode.getUser().getApplication().getStylesheetUrl() :
            (application != null ? application.getStylesheetUrl() : null);
    }
}
