package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.VersionedEntity;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * A login code is created when a user attempts to log in and an e-mail is sent
 */
@Entity
@Table(name = "login_codes")
public class LoginCode extends VersionedEntity {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id", updatable = false)
    private Client client;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @NotNull
    @Column(name = "expires", updatable = false)
    private Long expires;

    @NotNull
    @Column(name = "code", updatable = false)
    private String code;

    @NotNull
    @Column(name = "remember_me", updatable = false)
    private Boolean rememberMe;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", updatable = false)
    private ResponseType responseType;

    @NotEmpty
    @Column(name = "host", updatable = false)
    private String host;

    @URL
    @Lob
    @NotNull
    @Column(name = "redirect_uri", updatable = false)
    private String redirectUri;

    @Column(name = "scope", updatable = false)
    private String scope;

    @Column(name = "state", updatable = false)
    private String state;

    @Column(name = "used")
    private Boolean used;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpires() {
        return expires == null ? null : new Date(expires);
    }

    public void setExpires(Date expires) {
        this.expires = expires == null ? null : expires.getTime();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
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

    public String getScope() {
        return scope;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
