package com.oauth2cloud.server.rest.models;

public class ErrorModel implements HeadProperties {
    @Override
    public String getStylesheetUrl() {
        return null;
    }

    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
