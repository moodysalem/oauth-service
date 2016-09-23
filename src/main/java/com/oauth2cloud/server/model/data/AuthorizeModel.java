package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.api.ErrorResponse;
import com.oauth2cloud.server.model.db.Client;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Base class of the template models
 */
public abstract class AuthorizeModel implements HeadProperties {
    private String requestUrl, redirectUri, baseUri, state;
    private Client client;

    private boolean googleLogin;

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
        setBaseUri(ui.getBaseUri().toString());
    }

    private String getFragment(final ErrorResponse er) {
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
        final ErrorResponse er = new ErrorResponse(ErrorResponse.Type.access_denied, "User has cancelled the authorization", null);
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

    public boolean isGoogleLogin() {
        return googleLogin;
    }

    public void setGoogleLogin(boolean googleLogin) {
        this.googleLogin = googleLogin;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
}