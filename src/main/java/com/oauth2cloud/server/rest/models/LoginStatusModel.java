package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.LoginCookie;
import com.oauth2cloud.server.hibernate.model.TokenResponse;

public class LoginStatusModel {

    private LoginCookie loginCookie;
    private String targetOrigin;
    private TokenResponse tokenResponse;

    public LoginCookie getLoginCookie() {
        return loginCookie;
    }

    public void setLoginCookie(LoginCookie loginCookie) {
        this.loginCookie = loginCookie;
    }

    public String getTargetOrigin() {
        return targetOrigin;
    }

    public void setTargetOrigin(String targetOrigin) {
        this.targetOrigin = targetOrigin;
    }

    public TokenResponse getTokenResponse() {
        return tokenResponse;
    }

    public void setTokenResponse(TokenResponse tokenResponse) {
        this.tokenResponse = tokenResponse;
    }
}
