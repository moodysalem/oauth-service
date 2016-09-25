package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Audited
@Table(name = "accepted_scopes")
public class AcceptedScope extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "client_scope_id", updatable = false)
    private ClientScope clientScope;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
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
