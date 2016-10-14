package com.oauth2cloud.server.model.db;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_refresh_tokens")
public class UserRefreshToken extends UserToken {
    @Override
    public Long getTtl(Client client) {
        return client.getRefreshTokenTtl();
    }
}
