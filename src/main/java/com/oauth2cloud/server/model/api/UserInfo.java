package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.model.db.User;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * We only share and store e-mails and user ids
 */
public class UserInfo {
    public UserInfo(@NotNull final UUID userId, @NotNull final String email) {
        this.email = email;
        this.userId = userId;
    }

    public static UserInfo from(@NotNull User u) {
        return new UserInfo(u.getId(), u.getEmail());
    }

    @JsonProperty("email")
    private final String email;
    @JsonProperty("user_id")
    private final UUID userId;

    public String getEmail() {
        return email;
    }

    public UUID getUserId() {
        return userId;
    }
}
