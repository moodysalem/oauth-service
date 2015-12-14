package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.LoginCookie;

public class LoginStatusModel {

    private LoginCookie loginCookie;
    private String targetOrigin;
    private String authResponse;

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

    public String getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(String authResponse) {
        this.authResponse = authResponse;
    }
}
