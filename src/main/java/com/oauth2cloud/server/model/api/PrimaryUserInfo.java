package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.model.db.User;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * We only share and store e-mails and user ids
 */
public class PrimaryUserInfo {
    @JsonCreator
    public PrimaryUserInfo(
            @JsonProperty("user_id") final UUID userId,
            @JsonProperty("email") final String email,
            @JsonProperty("linked_users") final Set<UserInfo> linkedUsers
    ) {
        this.email = email;
        this.userId = userId;
        this.linkedUsers = linkedUsers;
    }

    public static PrimaryUserInfo from(@NotNull User user) {
        return new PrimaryUserInfo(user.getId(), user.getEmail(),
                user.getGroup() != null && user.getGroup().getUsers() != null ?
                        user.getGroup().getUsers()
                                .stream()
                                .filter(otherUser -> !otherUser.idMatch(user))
                                .map(UserInfo::from)
                                .collect(Collectors.toSet()) :
                        Collections.emptySet()
        );
    }

    @JsonProperty("email")
    private final String email;
    @JsonProperty("user_id")
    private final UUID userId;
    @JsonProperty("linked_users")
    private final Set<UserInfo> linkedUsers;

    public String getEmail() {
        return email;
    }

    public UUID getUserId() {
        return userId;
    }

    public Set<UserInfo> getLinkedUsers() {
        return linkedUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimaryUserInfo that = (PrimaryUserInfo) o;
        return Objects.equals(getEmail(), that.getEmail()) &&
                Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getLinkedUsers(), that.getLinkedUsers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getUserId(), getLinkedUsers());
    }
}
