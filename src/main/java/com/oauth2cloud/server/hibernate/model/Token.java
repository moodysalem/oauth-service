package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moodysalem.hibernate.model.BaseEntity;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Token extends BaseEntity {

    public enum Type {
        // the access token is used with resource servers to identify an authenticated user
        ACCESS,
        // these tokens are access tokens that last for a very short amount of time
        TEMPORARY,
        // the refresh token is shared only with the server as a method of getting new tokens
        REFRESH,
        // the permission is an internal token used for when the user is authenticated but not authorized
        PERMISSION,
        // the code is used for the authorization code flow
        CODE,
        // a token given in response to client credentials
        CLIENT
    }

    @ManyToOne
    @JoinColumn(name = "refreshTokenId")
    private Token refreshToken;

    @Column(name = "token")
    private String token;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "clientId")
    private Client client;

    @Column(name = "expires")
    private Date expires;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @Lob
    @Column(name = "redirectUri")
    private String redirectUri;

    @ManyToMany
    @JoinTable(
            name = "Token_AcceptedScope",
            joinColumns = @JoinColumn(name = "tokenId"),
            inverseJoinColumns = @JoinColumn(name = "acceptedScopeId")
    )
    @JsonIgnore
    private List<AcceptedScope> acceptedScopes;

    @ManyToMany
    @JoinTable(
            name = "Token_ClientScope",
            joinColumns = @JoinColumn(name = "tokenId"),
            inverseJoinColumns = @JoinColumn(name = "clientScopeId")
    )
    @JsonIgnore
    private List<ClientScope> clientScopes;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Lob
    @Column(name = "providerAccessToken")
    private String providerAccessToken;

    /**
     * @return a space delimited list of scope names, for approved client scopes that are active
     */
    public String getScope() {
        List<ClientScope> clientScopeList = null;

        if (getType().equals(Type.CLIENT)) {
            // client credential tokens only point to client scopes
            clientScopeList = getClientScopes().stream()
                    .filter(ClientScope::isApproved)
                    .filter(clientScope -> clientScope.getScope().isActive())
                    .collect(Collectors.toList());
        } else {
            // otherwise get the accepted scopes
            if (getAcceptedScopes() != null && getAcceptedScopes().size() > 0) {
                clientScopeList = getAcceptedScopes().stream().map(AcceptedScope::getClientScope)
                        .filter(ClientScope::isApproved)
                        .filter(clientScope -> clientScope.getScope().isActive())
                        .collect(Collectors.toList());
            }
        }

        if (clientScopeList == null) {
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
        return (getExpires().getTime() - System.currentTimeMillis()) / 1000L;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRandomToken(int length) {
        setToken(RandomStringUtils.randomAlphanumeric(length));
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<AcceptedScope> getAcceptedScopes() {
        return acceptedScopes;
    }

    public void setAcceptedScopes(List<AcceptedScope> acceptedScopes) {
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

    public List<ClientScope> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(List<ClientScope> clientScopes) {
        this.clientScopes = clientScopes;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getProviderAccessToken() {
        return providerAccessToken;
    }

    public void setProviderAccessToken(String providerAccessToken) {
        this.providerAccessToken = providerAccessToken;
    }
}
