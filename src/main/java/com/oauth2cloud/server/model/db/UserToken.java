package com.oauth2cloud.server.model.db;

import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_tokens")
public abstract class UserToken extends Token {
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @URL
    @NotNull
    @Column(name = "redirect_uri", updatable = false)
    private String redirectUri;

    @ManyToMany
    @JoinTable(
            name = "user_token_accepted_scopes",
            joinColumns = @JoinColumn(name = "user_token_id"),
            inverseJoinColumns = @JoinColumn(name = "accepted_scope_id")
    )
    private Set<AcceptedScope> acceptedScopes;

    @ManyToOne
    @JoinColumn(name = "login_code_id")
    private LoginCode loginCode;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<AcceptedScope> getAcceptedScopes() {
        return acceptedScopes;
    }

    public void setAcceptedScopes(Set<AcceptedScope> acceptedScopes) {
        this.acceptedScopes = acceptedScopes;
    }

    public LoginCode getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(LoginCode loginCode) {
        this.loginCode = loginCode;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public String getScope() {
        if (getAcceptedScopes() == null) {
            return "";
        }

        return getAcceptedScopes()
                .stream()
                .filter(as -> as != null)
                .map(AcceptedScope::getClientScope)
                .filter(cs -> cs != null)
                .map(ClientScope::getScope)
                .map(Scope::getName)
                .collect(Collectors.joining(" "));
    }
}
