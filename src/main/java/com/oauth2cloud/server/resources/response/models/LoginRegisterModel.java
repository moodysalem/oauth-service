package com.oauth2cloud.server.resources.response.models;

/**
 * This is the model that gets passed to Authorize.ftl
 */
public class LoginRegisterModel extends AuthorizeModel {
    private String loginError;
    private String registerError;
    private boolean registerSuccess;


    public String getLoginError() {
        return loginError;
    }

    public void setLoginError(String loginError) {
        this.loginError = loginError;
    }

    public String getRegisterError() {
        return registerError;
    }

    public void setRegisterError(String registerError) {
        this.registerError = registerError;
    }

    public boolean isRegisterSuccess() {
        return registerSuccess;
    }

    public void setRegisterSuccess(boolean registerSuccess) {
        this.registerSuccess = registerSuccess;
    }
}
