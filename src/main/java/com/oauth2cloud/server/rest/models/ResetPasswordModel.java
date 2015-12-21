package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.UserCode;

public class ResetPasswordModel implements HeadProperties {
    private Application application;
    private String error;
    private UserCode userCode;
    private boolean success;
    private String referrer;

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

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
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
