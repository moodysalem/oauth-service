package com.oauth2cloud.server.model.db;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "client_call_log")
public class ClientCallLog extends CallLog {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id", updatable = false)
    private Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
