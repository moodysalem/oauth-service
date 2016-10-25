package com.oauth2cloud.server.model.db;

import javax.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "client_tokens")
public class ClientToken extends Token {
    @ManyToMany
    @JoinTable(
            name = "client_token_client_scopes",
            joinColumns = @JoinColumn(name = "client_token_id"),
            inverseJoinColumns = @JoinColumn(name = "client_scope_id")
    )
    private Set<ClientScope> clientScopes;

    public Set<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(Set<ClientScope> clientScopes) {
        this.clientScopes = clientScopes;
    }

    @Override
    public String getScope() {
        return getClientScopes()
                .stream()
                .filter(cs -> cs != null)
                .map(ClientScope::getScope)
                .map(Scope::getName)
                .collect(Collectors.joining(" "));
    }

    @Override
    public Long getTtl(Client client) {
        return client.getTokenTtl() * 1000L;
    }
}
