package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.LoginCookie;
import com.oauth2cloud.server.hibernate.model.Token;

import java.util.List;

public class LoginStatusModel {

    private LoginCookie loginCookie;
    private List<Token> tokens;
    private String targetOrigin;

    public LoginCookie getLoginCookie() {
        return loginCookie;
    }

    public void setLoginCookie(LoginCookie loginCookie) {
        this.loginCookie = loginCookie;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public String getTargetOrigin() {
        return targetOrigin;
    }

    public void setTargetOrigin(String targetOrigin) {
        this.targetOrigin = targetOrigin;
    }
}
