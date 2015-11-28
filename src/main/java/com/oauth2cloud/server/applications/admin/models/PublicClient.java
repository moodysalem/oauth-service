package com.oauth2cloud.server.applications.admin.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth2cloud.server.hibernate.model.Client;

public class PublicClient {
    public PublicClient(Client client) {
        setClient(client);
    }

    @JsonIgnore
    private Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public long getId() {
        return client.getId();
    }

}
