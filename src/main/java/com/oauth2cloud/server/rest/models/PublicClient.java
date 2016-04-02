package com.oauth2cloud.server.rest.models;

import com.oauth2cloud.server.hibernate.model.Client;

import java.util.UUID;

public class PublicClient {
    private UUID id;
    private String name;
    private String identifier;
    private String secret;

    public PublicClient(Client client) {
        if (client == null) {
            throw new NullPointerException();
        }
        setId(client.getId());
        setName(client.getName());
        setIdentifier(client.getIdentifier());
        setSecret(client.getSecret());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
