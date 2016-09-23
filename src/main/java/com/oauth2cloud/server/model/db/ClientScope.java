package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Embeddable
public class ClientScope extends BaseEntity {
    public enum Priority {
        // ALWAYS is the highest level, the user is not asked nor shown the permission when logging in
        ALWAYS,
        // REQUIRE is the middle level, the user must accept this permission to utilize the client
        REQUIRE,
        // ASK is the lowest level, the user has the option of accepting this permission or not
        ASK
    }

    @ManyToOne
    @JoinColumn(name = "scope_id", updatable = false)
    private Scope scope;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientScope that = (ClientScope) o;
        return Objects.equals(getScope(), that.getScope()) &&
                getPriority() == that.getPriority() &&
                Objects.equals(getReason(), that.getReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScope(), getPriority(), getReason());
    }
}
