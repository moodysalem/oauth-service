package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;
import com.leaguekit.util.RandomStringUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Token extends BaseEntity {

    public enum Type {
        LOGIN, PERMISSION, REFRESH, CODE;

    }
    @Column(name = "token")
    private String token;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
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
    private List<AcceptedScope> acceptedScopes;

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
