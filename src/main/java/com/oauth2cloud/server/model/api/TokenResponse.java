package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.model.db.Token;
import com.oauth2cloud.server.model.db.TokenType;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.rest.util.QueryString;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.UUID;

/**
 * The OAauth2 spec for a token response is represented by this object
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    public static final String BEARER = "bearer";

    public static TokenResponse from(final Token accessToken) {
        // this should be called with access tokens
        if (!TokenType.ACCESS.equals(accessToken.getType())) {
            throw new IllegalArgumentException();
        }

        final TokenResponse tr = new TokenResponse();
        tr.setAccessToken(accessToken.getToken());
        tr.setExpiresIn(accessToken.getExpiresIn());
        if (accessToken.getRefreshToken() != null) {
            tr.setRefreshToken(accessToken.getRefreshToken().getToken());
        }
        tr.setScope(accessToken.getScope());
        tr.setTokenType(BEARER);
        tr.setClientId(accessToken.getClient().getCredentials().getId());
        tr.setUser(accessToken.getUser());
        tr.setApplicationId(accessToken.getClient().getApplication().getId());
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

    @JsonProperty("user")
    private UserInfo user;

    @JsonProperty("application_id")
    private UUID applicationId;

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
        this.user = UserInfo.from(user);
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UserInfo getUser() {
        return user;
    }

    public String toFragment(final String state) {
        final MultivaluedMap<String, String> params = new MultivaluedHashMap<>();

        params.putSingle("access_token", getAccessToken());
        params.putSingle("token_type", BEARER);
        if (state != null) {
            params.putSingle("state", state);
        }
        params.putSingle("expires_in", Long.toString(getExpiresIn()));
        params.putSingle("scope", getScope());

        return QueryString.mapToQueryString(params);
    }
}
