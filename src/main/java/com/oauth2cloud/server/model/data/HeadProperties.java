package com.oauth2cloud.server.model.data;

/**
 * Models should implement this to ensure that application styles are passed forward where appropriate
 */
public interface HeadProperties {

    String getStylesheetUrl();

    String getFaviconUrl();

}
