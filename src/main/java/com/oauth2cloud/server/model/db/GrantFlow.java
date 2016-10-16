package com.oauth2cloud.server.model.db;

/**
 * Described here:
 * https://tools.ietf.org/html/rfc6749#section-1.3.2
 */
public enum GrantFlow {
    IMPLICIT,
    CODE,
    RESOURCE_OWNER_CREDENTIALS,
    CLIENT_CREDENTIALS
}
