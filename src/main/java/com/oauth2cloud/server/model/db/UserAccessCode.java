package com.oauth2cloud.server.model.db;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_access_code")
public class UserAccessCode extends UserToken {
    private static final long ONE_MINUTE_MS = 1000L * 60L;

    @Override
    public Long getTtl(Client client) {
        return ONE_MINUTE_MS;
    }
}
