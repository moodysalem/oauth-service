package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class AcceptedScope extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "clientScopeId")
    private ClientScope clientScope;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ClientScope getClientScope() {
        return clientScope;
    }

    public void setClientScope(ClientScope clientScope) {
        this.clientScope = clientScope;
    }
}
