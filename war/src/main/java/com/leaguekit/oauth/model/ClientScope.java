package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.*;

@Entity
public class ClientScope extends BaseEntity {

    public enum Priority {
        ALWAYS,
        REQUIRE,
        ASK
    }

    @ManyToOne
    @JoinColumn(name = "clientId")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "scopeId")
    private Scope scope;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority;

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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
