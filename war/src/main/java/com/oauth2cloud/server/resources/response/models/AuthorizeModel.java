package com.oauth2cloud.server.resources.response.models;

import com.oauth2cloud.server.model.Client;

import javax.ws.rs.container.ContainerRequestContext;

public class AuthorizeModel {
    private String requestUrl;
    private String baseUrl;
    private Client client;
    private String loginError;
    private String registerError;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getLoginError() {
        return loginError;
    }

    public void setLoginError(String loginError) {
        this.loginError = loginError;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setURLs(ContainerRequestContext containerRequestContext) {
        if (containerRequestContext == null) {
            return;
        }
        setRequestUrl(containerRequestContext.getUriInfo().getRequestUri().toString());
        setBaseUrl(containerRequestContext.getUriInfo().getBaseUri().toString());
    }

    public String getRegisterError() {
        return registerError;
    }

    public void setRegisterError(String registerError) {
        this.registerError = registerError;
    }
}