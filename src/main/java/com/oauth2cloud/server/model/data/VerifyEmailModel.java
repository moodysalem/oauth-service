package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.UserCode;

public class VerifyEmailModel implements HeadProperties {
    public enum AlertLevel {
        danger, warning, success
    }

    public VerifyEmailModel(UserCode userCode, String message, AlertLevel alertLevel) {
        this.userCode = userCode;
        this.message = message;
        this.alertLevel = alertLevel;
    }

    private final UserCode userCode;
    private final String message;
    private final AlertLevel alertLevel;

    public UserCode getUserCode() {
        return userCode;
    }

    public String getMessage() {
        return message;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    @Override
    public String getStylesheetUrl() {
        return getUserCode() != null ? getUserCode().getUser().getApplication().getStylesheetUrl() : null;
    }

    @Override
    public String getFaviconUrl() {
        return getUserCode() != null ? getUserCode().getUser().getApplication().getFaviconUrl() : null;
    }
}
