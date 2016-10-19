package com.oauth2cloud.server.model.api;

import java.util.ResourceBundle;

public abstract class Version {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("APIVersion");
    private static final String VERSION = BUNDLE.getString("api.version");

    public static String get() {
        return VERSION;
    }
}
