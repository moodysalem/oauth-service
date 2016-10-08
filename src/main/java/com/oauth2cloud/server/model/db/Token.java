package com.oauth2cloud.server.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moodysalem.hibernate.model.VersionedEntity;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Entity
@Audited
@Table(name = "tokens")
public class Token extends VersionedEntity {
    @ManyToOne
    @JoinColumn(name = "refresh_token_id", updatable = false)
    private Token refreshToken;

    @Column(name = "token", updatable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "client_id", updatable = false)
    private Client client;

    @NotNull
    @Column(name = "expires")
    private Long expires;

    @Column(name = "type", updatable = false)
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Lob
    @Column(name = "redirect_uri", updatable = false)
    private String redirectUri;

    @ManyToMany
    @JoinTable(
            name = "token_accepted_scopes",
            joinColumns = @JoinColumn(name = "token_id"),
            inverseJoinColumns = @JoinColumn(name = "accepted_scope_id")
    )
    private Set<AcceptedScope> acceptedScopes;

    @ManyToMany
    @JoinTable(
            name = "token_client_scopes",
            joinColumns = @JoinColumn(name = "token_id"),
            inverseJoinColumns = @JoinColumn(name = "client_scope_id")
    )
    private Set<ClientScope> clientScopes;

    /**
     * @return a space delimited list of scope names, for approved client scopes that are active
     */
    public String getScope() {
        Set<ClientScope> clientScopeList = null;

        if (getType().equals(TokenType.CLIENT)) {
            // client credential tokens only point to client scopes
            clientScopeList = getClientScopes().stream()
                    .collect(Collectors.toSet());
        } else {
            // otherwise get the accepted scopes
            if (getAcceptedScopes() != null && getAcceptedScopes().size() > 0) {
                clientScopeList = getAcceptedScopes().stream().map(AcceptedScope::getClientScope)
                        .collect(Collectors.toSet());
            }
        }

        if (clientScopeList == null || clientScopeList.isEmpty()) {
            return "";
        }

        return clientScopeList.stream()
                .map(ClientScope::getScope).map(Scope::getName)
                .collect(Collectors.joining(" "));
    }

    /**
     * @return the number of seconds remaining on the token
     */
    public Long getExpiresIn() {
        return (expires - System.currentTimeMillis()) / 1000L;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRandomToken(int length) {
        setToken(randomAlphanumeric(length));
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpires() {
        return expires == null ? null : new Date(expires);
    }

    public void setExpires(Date expires) {
        this.expires = expires == null ? null : expires.getTime();
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public Set<AcceptedScope> getAcceptedScopes() {
        return acceptedScopes;
    }

    public void setAcceptedScopes(Set<AcceptedScope> acceptedScopes) {
        this.acceptedScopes = acceptedScopes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(Token refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Set<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(Set<ClientScope> clientScopes) {
        this.clientScopes = clientScopes;
    }

    /**
     * Helper function to calculate when a token should expire based on the client's TTL
     *
     * @param client for which the token is being generated
     * @return when the token should expire
     */
    public static Date getExpires(final Client client, final TokenType type) {
        if (TokenType.REFRESH.equals(type)) {
            if (client.getRefreshTokenTtl() == null) {
                throw new IllegalArgumentException();
            }
            return new Date(client.getRefreshTokenTtl() * 1000L);
        }

        if (type.getFixedTtl() != null) {
            return new Date(type.getFixedTtl() * 1000L);
        }

        return new Date(System.currentTimeMillis() + client.getTokenTtl());
    }
}
