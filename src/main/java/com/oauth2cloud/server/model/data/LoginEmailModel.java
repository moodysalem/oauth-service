package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCode;
import com.oauth2cloud.server.rest.util.QueryString;

import javax.ws.rs.core.Form;

public class LoginEmailModel {
    public LoginEmailModel(final Client client, final LoginCode loginCode, final String responseType,
                           final String redirectUri, final String state, final String scope) {
        this.client = client;
        this.loginCode = loginCode;
        this.responseType = responseType;
        this.redirectUri = redirectUri;
        this.state = state;
        this.scope = scope;
    }

    private final Client client;
    private final LoginCode loginCode;
    private final String responseType, redirectUri, state, scope;

    public String getQueryString(final String code) {
        final Form form = new Form();
        form.param("redirect_uri", getRedirectUri())
                .param("response_type", getResponseType())
                .param("login_code", code);

        if (getState() != null) {
            form.param("state", getState());
        }

        if (getScope() != null) {
            form.param("scope", getScope());
        }

        return QueryString.mapToQueryString(form.asMap());
    }

    public String getResponseType() {
        return responseType;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getState() {
        return state;
    }

    public String getScope() {
        return scope;
    }

    public Client getClient() {
        return client;
    }

    public LoginCode getLoginCode() {
        return loginCode;
    }
}
