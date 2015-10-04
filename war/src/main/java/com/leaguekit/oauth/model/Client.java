package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Client extends BaseEntity {

    public enum GrantFlow {
        IMPLICIT,
        CODE;
    }

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "applicationId")
    private Application application;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "secret")
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
}
