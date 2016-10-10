package com.oauth2cloud.server.model.db;

public enum ResponseType {
    token(TokenType.ACCESS),
    code(TokenType.CODE);

    private final TokenType createsTokenType;

    ResponseType(TokenType createsTokenType) {
        this.createsTokenType = createsTokenType;
    }

    public TokenType getCreatesTokenType() {
        return createsTokenType;
    }
}
