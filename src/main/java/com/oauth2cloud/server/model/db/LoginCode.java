package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import com.oauth2cloud.server.rest.util.QS;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Form;
import java.util.Date;
import java.util.Set;

/**
 * A login code is created when a user attempts to log in and an e-mail is sent
 */
@Entity
@Immutable
@Table(name = "login_code")
public class LoginCode extends BaseEntity {
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
    @Column(name = "response_type", updatable = false)
    private String responseType;

    @NotNull
    @Column(name = "redirect_uri", updatable = false)
    private String redirectUri;

    @Column(name = "state", updatable = false)
    private String state;

    @Column(name = "scope", updatable = false)
    private Set<String> scopes;

    @NotNull
    @Column(name = "code", updatable = false)
    private String code;

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

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
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

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getQueryString() {
        final Form form = new Form();
        form.param("redirect_uri", getRedirectUri())
                .param("response_type", getResponseType())
                .param("login_code", getCode());

        if (getState() != null) {
            form.param("state", getState());
        }

        if (getScopes() != null) {
            getScopes().forEach(scope -> form.param("scope", scope));
        }

        return QS.mapToQueryString(form.asMap());
    }
}
