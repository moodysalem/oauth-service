package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The OAauth2 spec for an token response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

    public static final String BEARER = "bearer";

    public static TokenResponse from(Token accessToken) {
        TokenResponse tr = new TokenResponse();
        tr.setAccessToken(accessToken.getToken());
        tr.setExpiresIn(accessToken.getExpiresIn());
        if (accessToken.getRefreshToken() != null) {
            tr.setRefreshToken(accessToken.getRefreshToken().getToken());
        }
        tr.setScope(accessToken.getScope());
        tr.setTokenType(BEARER);
        tr.setClientId(accessToken.getClient().getIdentifier());
        tr.setUser(accessToken.getUser());
        tr.setApplicationId(accessToken.getClient().getApplication().getId());
        tr.setProviderName(accessToken.getProvider().name());
        tr.setProviderAccessToken(accessToken.getProviderAccessToken());
        return tr;
    }

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("user_details")
    private UserDetails userDetails;

    @JsonProperty("application_id")
    private Long applicationId;

    @JsonProperty("provider")
    private String providerName;

    @JsonProperty("provider_access_token")
    private String providerAccessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setUser(User user) {
        this.userDetails = UserDetails.from(user);
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderAccessToken() {
        return providerAccessToken;
    }

    public void setProviderAccessToken(String providerAccessToken) {
        this.providerAccessToken = providerAccessToken;
    }
}
