package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.Application;

public class ResetPasswordModel implements HeadProperties {
    private final Application application;
    private final String error;
    private final VerificationCode verificationCode;
    private final boolean success;
    private final String referrer;

    public ResetPasswordModel(Application application, String error, VerificationCode verificationCode, boolean success, String referrer) {
        this.application = application;
        this.error = error;
        this.verificationCode = verificationCode;
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

    public VerificationCode getVerificationCode() {
        return verificationCode;
    }

    public String getReferrer() {
        return referrer;
    }

    @Override
    public String getStylesheetUrl() {
        return verificationCode != null ? verificationCode.getUser().getApplication().getStylesheetUrl() :
                (application != null ? application.getStylesheetUrl() : null);
    }

    @Override
    public String getFaviconUrl() {
        return verificationCode != null ? verificationCode.getUser().getApplication().getFaviconUrl() :
                (application != null ? application.getFaviconUrl() : null);
    }
}
