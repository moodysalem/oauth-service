package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Client extends BaseEntity {

    public enum GrantFlow {
        IMPLICIT,
        CODE,
        RESOURCE_OWNER_CREDENTIALS,
        CLIENT_CREDENTIALS,
        TEMPORARY_TOKEN
    }


    public enum Type {
        CONFIDENTIAL,
        PUBLIC;
    }

    @NotBlank
    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne
    @JoinColumn(name = "applicationId")
    private Application application;

    @Column(name = "identifier", updatable = false)
    private String identifier;

    @Column(name = "secret", updatable = false)
    private String secret;

    @ElementCollection
    @CollectionTable(name = "Client_URI", joinColumns = @JoinColumn(name = "clientId"))
    @Column(name = "uri")
    private Set<String> uris;

    @ElementCollection
    @CollectionTable(name = "Client_Flow", joinColumns = @JoinColumn(name = "clientId"))
    @Enumerated(EnumType.STRING)
    @Column(name = "flow")
    private Set<GrantFlow> flows;

    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<ClientScope> clientScopes;

    @Column(name = "tokenTtl")
    private Long tokenTtl;

    @Column(name = "refreshTokenTtl")
    private Long refreshTokenTtl;

    @ManyToOne
    @JoinColumn(name = "creatorId", updatable = false)
    @JsonIgnore
    private User creator;


    @JsonIgnore
    @Column(name = "deleted")
    private boolean deleted;

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

    public List<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(List<ClientScope> clientScopes) {
        this.clientScopes = clientScopes;
    }

    public Long getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Long tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
