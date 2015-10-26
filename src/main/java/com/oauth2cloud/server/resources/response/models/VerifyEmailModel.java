package com.oauth2cloud.server.resources.response.models;

import com.oauth2cloud.server.model.UserCode;

public class VerifyEmailModel {
    public UserCode getUserCode() {
        return userCode;
    }

    public void setUserCode(UserCode userCode) {
        this.userCode = userCode;
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
