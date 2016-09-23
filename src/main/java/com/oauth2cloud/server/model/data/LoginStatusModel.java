package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.LoginCookie;

public class LoginStatusModel {
    public LoginStatusModel(LoginCookie loginCookie, String targetOrigin, TokenResponse tokenResponse) {
        this.loginCookie = loginCookie;
        this.targetOrigin = targetOrigin;
        this.tokenResponse = tokenResponse;
    }

    private final LoginCookie loginCookie;
    private final String targetOrigin;
    private final TokenResponse tokenResponse;

    public LoginCookie getLoginCookie() {
        return loginCookie;
    }

    public String getTargetOrigin() {
        return targetOrigin;
    }

    public TokenResponse getTokenResponse() {
        return tokenResponse;
    }
}
