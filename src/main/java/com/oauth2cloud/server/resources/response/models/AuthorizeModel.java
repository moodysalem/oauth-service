package com.oauth2cloud.server.resources.response.models;

import com.oauth2cloud.server.hibernate.model.Client;
import com.oauth2cloud.server.hibernate.model.ErrorResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Base class of the template models
 */
public abstract class AuthorizeModel {
    private String requestUrl;
    private Client client;
    private String redirectUri;
    private String state;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setURLs(ContainerRequestContext containerRequestContext) {
        if (containerRequestContext == null) {
            return;
        }
        UriInfo ui = containerRequestContext.getUriInfo();
        setRequestUrl(ui.getRequestUri().toString());
    }

    private String getFragment(ErrorResponse er) {
        if (getState() == null || getState().length() == 0) {
            return er.toString();
        }

        if (er == null) {
            return "state=" + getState();
        } else {
            return er.toString() + "&state=" + getState();
        }
    }

    public String getCancelUrl() {
        ErrorResponse er = new ErrorResponse();
        er.setError(ErrorResponse.Type.access_denied);
        er.setErrorDescription("User has cancelled the authorization");
        try {
            return UriBuilder.fromUri(new URI(getRedirectUri())).fragment(getFragment(er)).build().toString();
        } catch (Exception ignored) {
        }
        return getRedirectUri();
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}