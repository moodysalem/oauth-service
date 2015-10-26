package com.oauth2cloud.server.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.*;
import java.util.Comparator;

@Entity
public class ClientScope extends BaseEntity implements Comparable<ClientScope> {


    @Override
    public int compareTo(ClientScope o) {
        // nulls sort first
        if (o == null) {
            return 1;
        }
        Priority p1 = getPriority();
        Priority p2 = o.getPriority();
        if (p1 == null && p2 == null) {
            return 0;
        }
        if (p2 == null) {
            return 1;
        }
        if (p1 == null) {
            return -1;
        }
        if (p1.equals(p2)) {
            return 0;
        }
        if (p1.equals(Priority.ALWAYS) || p2.equals(Priority.ASK)) {
            return -1;
        }
        if (p2.equals(Priority.ALWAYS) || p1.equals(Priority.ASK)) {
            return 1;
        }
        return 0;
    }

    public enum Priority {
        // ALWAYS is the highest level, the user is not asked nor shown the permission when logging in
        ALWAYS,
        // REQUIRE is the middle level, the user must accept this permission to utilize the client
        REQUIRE,
        // ASK is the lowest level, the user has the option of accepting this permission or not
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

    @Lob
    @Column(name = "reason")
    private String reason;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
