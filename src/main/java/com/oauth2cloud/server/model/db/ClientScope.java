package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.VersionedEntity;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Audited
@Table(name = "client_scopes")
public class ClientScope extends VersionedEntity {
    public enum Priority {
        // ALWAYS is the highest level, the user is not asked for nor shown the permission when logging in
        ALWAYS,
        // REQUIRE is the middle level, the user must accept this permission to log in
        REQUIRE,
        // ASK is the lowest level, the user has the option of accepting this permission or not
        ASK
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id", updatable = false)
    private Client client;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "scope_id", updatable = false)
    private Scope scope;

    @NotNull
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Lob
    @Column(name = "reason")
    private String reason;

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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
