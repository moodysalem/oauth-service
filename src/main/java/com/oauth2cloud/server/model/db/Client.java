package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.VersionedEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
public class Client extends VersionedEntity {
    public enum GrantFlow {
        IMPLICIT,
        CODE,
        RESOURCE_OWNER_CREDENTIALS,
        CLIENT_CREDENTIALS,
        TEMPORARY_TOKEN
    }

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "confidential", nullable = false)
    private boolean confidential;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "identifier", updatable = false)),
            @AttributeOverride(name = "secret", column = @Column(name = "secret", updatable = false))
    })
    private ClientCredentials credentials;

    @ElementCollection
    @CollectionTable(name = "client_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "uri")
    private Set<String> uris;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_flows", joinColumns = @JoinColumn(name = "client_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "flow")
    private Set<GrantFlow> flows;

    @Embedded
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_scopes", joinColumns = @JoinColumn(name = "client_id"))
    private Set<ClientScope> clientScopes;

    @Column(name = "token_ttl")
    private Long tokenTtl;

    @Column(name = "refresh_token_ttl")
    private Long refreshTokenTtl;

    @ManyToOne
    @JoinColumn(name = "creator_id", updatable = false)
    private User creator;

    @Column(name = "active")
    private boolean active;

    public ClientCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(ClientCredentials credentials) {
        this.credentials = credentials;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Set<String> getUris() {
        return uris;
    }

    public void setUris(Set<String> uris) {
        this.uris = uris;
    }

    public Set<GrantFlow> getFlows() {
        return flows;
    }

    public void setFlows(Set<GrantFlow> flows) {
        this.flows = flows;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(Set<ClientScope> clientScopes) {
        this.clientScopes = clientScopes;
    }

    public Long getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Long tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public Long getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Long refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getCreatorEmail() {
        return creator != null ? creator.getEmail() : null;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
