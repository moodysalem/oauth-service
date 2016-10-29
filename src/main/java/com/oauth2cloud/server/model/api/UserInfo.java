package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.model.db.User;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * We only share and store e-mails and user ids
 */
public class UserInfo {
    public UserInfo(
            @JsonProperty("user_id") final UUID userId,
            @JsonProperty("email") final String email
    ) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(getEmail(), userInfo.getEmail()) &&
                Objects.equals(getUserId(), userInfo.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getUserId());
    }
}
