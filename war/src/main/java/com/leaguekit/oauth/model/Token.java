package com.leaguekit.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leaguekit.hibernate.model.BaseEntity;
import com.leaguekit.util.RandomStringUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Token extends BaseEntity {

    public enum Type {
        // the access token is used with resource servers to identify an authenticated user
        ACCESS,
        // the refresh token is shared only with the server as a method of getting new tokens
        REFRESH,
        // the permission is an internal token used for when the user is authenticated but not authorized
        PERMISSION,
        // the code is used for
        CODE
    }

    @Column(name = "token")
    private String token;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "clientId")
    private Client client;

    @Column(name = "expires")
    private Date expires;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToMany
    @JoinTable(
        name = "Token_AcceptedScope",
        joinColumns = @JoinColumn(name = "tokenId"),
        inverseJoinColumns = @JoinColumn(name = "acceptedScopeId")
    )
    @JsonIgnore
    private List<AcceptedScope> acceptedScopes;

    /**
     * @return a space delimited list of scope names
     */
    public String getScope() {
        if (getAcceptedScopes() == null) {
            return "";
        }
        return getAcceptedScopes().stream()
            .map(AcceptedScope::getClientScope).map(ClientScope::getScope).map(Scope::getName)
            .collect(Collectors.joining(" "));
    }

    /**
     * @return the number of seconds remaining on the token
     */
    public Long getExpiresIn() {
        return (getExpires().getTime() - (new Date()).getTime()) / 1000L;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRandomToken(int length) {
        setToken(RandomStringUtil.randomAlphaNumeric(length));
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<AcceptedScope> getAcceptedScopes() {
        return acceptedScopes;
    }

    public void setAcceptedScopes(List<AcceptedScope> acceptedScopes) {
        this.acceptedScopes = acceptedScopes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
