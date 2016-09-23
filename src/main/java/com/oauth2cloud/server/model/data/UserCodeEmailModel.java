package com.oauth2cloud.server.model.data;

public class UserCodeEmailModel implements HeadProperties {
    private final VerificationCode verificationCode;
    private final String url;

    public UserCodeEmailModel(VerificationCode verificationCode, String url) {
        this.verificationCode = verificationCode;
        this.url = url;
    }

    public VerificationCode getVerificationCode() {
        return verificationCode;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getStylesheetUrl() {
        return verificationCode != null ? verificationCode.getUser().getApplication().getStylesheetUrl() : null;
    }

    @Override
    public String getFaviconUrl() {
        return verificationCode != null ? verificationCode.getUser().getApplication().getFaviconUrl() : null;
    }
}
