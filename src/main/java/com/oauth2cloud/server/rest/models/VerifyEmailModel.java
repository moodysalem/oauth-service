package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.UserCode;

public class VerifyEmailModel implements HeadProperties {
    public UserCode getUserCode() {
        return userCode;
    }

    public void setUserCode(UserCode userCode) {
        this.userCode = userCode;
    }

    @Override
    public String getStylesheetUrl() {
        return userCode != null ? userCode.getUser().getApplication().getStylesheetUrl() : null;
    }

    public enum AlertLevel {
        danger, warning, success
    }

    private UserCode userCode;
    private String message;

    private AlertLevel alertLevel;

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
