package com.oauth2cloud.server.applications.oauth.models;

import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Client;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Base class of the template models
 */
public abstract class AuthorizeModel implements HeadProperties {
    private String requestUrl;
    private Client client;
    private String redirectUri;
    private String baseUri;
    private String state;

    private int loginButtonSize;
    private boolean amazonLogin;
    private boolean googleLogin;
    private boolean facebookLogin;

    public void setProviders(Application application) {
        int numLogins = 0;
        if (application.getAmazonClientId() != null && application.getAmazonClientSecret() != null) {
            numLogins++;
            setAmazonLogin(true);
        }
        if (application.getFacebookAppId() != null && application.getFacebookAppSecret() != null) {
            numLogins++;
            setFacebookLogin(true);
        }
        if (application.getGoogleClientId() != null && application.getGoogleClientSecret() != null) {
            numLogins++;
            setGoogleLogin(true);
        }
        setLoginButtonSize((numLogins > 0) ? 12 / numLogins : 0);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
        if (client != null) {
            setProviders(client.getApplication());
        }
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

    public int getLoginButtonSize() {
        return loginButtonSize;
    }

    public void setLoginButtonSize(int loginButtonSize) {
        this.loginButtonSize = loginButtonSize;
    }

    public boolean isAmazonLogin() {
        return amazonLogin;
    }

    public void setAmazonLogin(boolean amazonLogin) {
        this.amazonLogin = amazonLogin;
    }

    public boolean isGoogleLogin() {
        return googleLogin;
    }

    public void setGoogleLogin(boolean googleLogin) {
        this.googleLogin = googleLogin;
    }

    public boolean isFacebookLogin() {
        return facebookLogin;
    }

    public void setFacebookLogin(boolean facebookLogin) {
        this.facebookLogin = facebookLogin;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
}