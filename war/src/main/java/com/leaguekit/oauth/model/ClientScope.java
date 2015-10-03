package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class ClientScope extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "clientId")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "scopeId")
    private Scope scope;

    @Column(name = "requiresPermission")
    private boolean requiresPermission;

    public boolean isRequiresPermission() {
        return requiresPermission;
    }

    public void setRequiresPermission(boolean requiresPermission) {
        this.requiresPermission = requiresPermission;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
