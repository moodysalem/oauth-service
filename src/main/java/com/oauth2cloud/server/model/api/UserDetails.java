package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.model.db.User;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * We only share and store e-mails and user ids
 */
public class UserDetails {
    public static UserDetails from(@NotNull User u) {
        final UserDetails ud = new UserDetails();
        ud.setEmail(u.getEmail());
        ud.setUserId(u.getId());
        return ud;
    }

    @JsonProperty("email")
    private String email;
    @JsonProperty("user_id")
    private UUID userId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
