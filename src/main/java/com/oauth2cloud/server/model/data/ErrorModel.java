package com.oauth2cloud.server.model.data;

/**
 * This model is used in the Error.ftl template
 */
public class ErrorModel implements HeadProperties {
    @Override
    public String getStylesheetUrl() {
        return null;
    }

    @Override
    public String getFaviconUrl() {
        return null;
    }

    private final String error;

    public ErrorModel(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
