package com.oauth2cloud.server.model.db;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * A login code is created when a user attempts to log in and an e-mail is sent
 */
@Entity
@Table(name = "login_codes")
public class LoginCode extends Token {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @URL
    @NotNull
    @Column(name = "redirect_uri", updatable = false)
    private String redirectUri;

    @NotNull
    @Column(name = "remember_me", updatable = false)
    private Boolean rememberMe;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", updatable = false)
    private ResponseType responseType;

    @URL
    @NotEmpty
    @Column(name = "base_uri", updatable = false)
    private String baseUri;

    @Column(name = "scope", updatable = false)
    private String scope;

    @Column(name = "state", updatable = false)
    private String state;

    @NotNull
    @Column(name = "used")
    private Boolean used;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public Long getTtl(Client client) {
        return Long.valueOf(client.getLoginCodeTtl()) * 1000L;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
