package com.oauth2cloud.server.model.db;

public enum TokenType {
    // the access token is used with resource servers to identify an authenticated user
    ACCESS(null),
    // the refresh token is shared only with confidential clients as a method of getting new tokens
    REFRESH(null),
    // the code is used for the authorization code flow
    CODE(null),
    // a token given in response to client credentials
    CLIENT(null),
    // these tokens are access tokens that last for a very short amount of time
    TEMPORARY(300L),
    // the permission is an internal token used for when the user is authenticated but not authorized for all scopes
    PERMISSION(300L);

    private final Long fixedTtl;

    TokenType(final Long fixedTtl) {
        this.fixedTtl = fixedTtl;
    }

    public Long getFixedTtl() {
        return fixedTtl;
    }
}
