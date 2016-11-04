package com.oauth2cloud.server.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "clients")
public class Client extends VersionedEntity {
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
            @AttributeOverride(name = "id", column = @Column(name = "identifier")),
            @AttributeOverride(name = "secret", column = @Column(name = "secret"))
    })
    private ClientCredentials credentials;

    @ElementCollection
    @CollectionTable(name = "client_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "uri")
    private Set<String> uris;

    @Column(name = "flow")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_flows", joinColumns = @JoinColumn(name = "client_id"))
    @Enumerated(EnumType.STRING)
    private Set<GrantFlow> flows;

    @Min(0)
    @NotNull
    @Column(name = "token_ttl")
    private Integer tokenTtl;

    @Min(0)
    @Column(name = "refresh_token_ttl")
    private Integer refreshTokenTtl;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "creator_id", updatable = false)
    private User creator;

    @Min(60)
    @NotNull
    @Column(name = "login_code_ttl")
    private Integer loginCodeTtl;

    @Column(name = "show_prompt_no_scopes")
    private boolean showPromptNoScopes;

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

    public Integer getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Integer tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public Integer getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Integer refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public boolean isShowPromptNoScopes() {
        return showPromptNoScopes;
    }

    public void setShowPromptNoScopes(boolean showPromptNoScopes) {
        this.showPromptNoScopes = showPromptNoScopes;
    }

    public Integer getLoginCodeTtl() {
        return loginCodeTtl;
    }

    public void setLoginCodeTtl(Integer loginCodeTtl) {
        this.loginCodeTtl = loginCodeTtl;
    }
}
