package com.oauth2cloud.server.model.data;

public class VerifyEmailModel implements HeadProperties {
    public enum AlertLevel {
        danger, warning, success
    }

    public VerifyEmailModel(VerificationCode verificationCode, String message, AlertLevel alertLevel) {
        this.verificationCode = verificationCode;
        this.message = message;
        this.alertLevel = alertLevel;
    }

    private final VerificationCode verificationCode;
    private final String message;
    private final AlertLevel alertLevel;

    public VerificationCode getVerificationCode() {
        return verificationCode;
    }

    public String getMessage() {
        return message;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    @Override
    public String getStylesheetUrl() {
        return getVerificationCode() != null ? getVerificationCode().getUser().getApplication().getStylesheetUrl() : null;
    }

    @Override
    public String getFaviconUrl() {
        return getVerificationCode() != null ? getVerificationCode().getUser().getApplication().getFaviconUrl() : null;
    }
}
