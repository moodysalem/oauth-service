package com.oauth2cloud.server.model.db;

public enum GrantFlow {
    IMPLICIT,
    CODE,
    RESOURCE_OWNER_CREDENTIALS,
    CLIENT_CREDENTIALS,
    TEMPORARY_TOKEN
}
