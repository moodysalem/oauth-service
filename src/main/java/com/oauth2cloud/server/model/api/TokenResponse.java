package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.model.db.Token;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.model.db.UserAccessToken;
import com.oauth2cloud.server.model.db.UserToken;
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

    public static TokenResponse from(final Token token) {
        final TokenResponse tr = new TokenResponse();
        tr.setAccessToken(token.getToken());
        tr.setExpiresIn(token.getExpiresIn());

        if (token instanceof UserToken) {
            final UserToken userToken = (UserToken) token;
            tr.setUser(userToken.getUser());
            if (userToken instanceof UserAccessToken) {
                final UserAccessToken uat = (UserAccessToken) token;
                tr.setRefreshToken(uat.getRefreshToken() != null ? uat.getRefreshToken().getToken() : null);
            }
        }

        tr.setScope(token.getScope());
        tr.setClientId(token.getClient().getCredentials().getId());
        tr.setApplicationId(token.getClient().getApplication().getId());
        return tr;
    }

    @JsonProperty("access_token")
    private String accessToken;


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

    @JsonProperty("token_type")
    public String getTokenType() {
        return BEARER;
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
