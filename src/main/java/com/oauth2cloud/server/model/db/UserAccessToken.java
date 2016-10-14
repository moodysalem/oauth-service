package com.oauth2cloud.server.model.db;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_access_tokens")
public class UserAccessToken extends UserToken {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "refresh_token_id")
    private UserRefreshToken refreshToken;

    public UserRefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(UserRefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public Long getTtl(Client client) {
        return client.getTokenTtl();
    }
}
