package com.oauth2cloud.server.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_access_codes")
public class UserAccessCode extends UserToken {
    private static final long FIVE_MINUTES_MS = 1000L * 60L * 5L;

    @NotNull
    @Column(name = "used")
    private boolean used;

    @Override
    public Long getTtl(Client client) {
        return FIVE_MINUTES_MS;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
