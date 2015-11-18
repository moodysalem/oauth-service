package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A simple wrapper around a user that is communicated to clients
 */
public class UserDetails {

    public UserDetails(User u) {
        if (u == null) {
            throw new NullPointerException("Null user passed to UserDetails constructor.");
        }
        this.user = u;
    }

    @JsonIgnore
    private User user;

    @JsonProperty("email")
    public String getEmail() {
        return user.getEmail();
    }

    @JsonProperty("first_name")
    public String getFirstName() {
        return user.getFirstName();
    }

    @JsonProperty("last_name")
    public String getLastName() {
        return user.getLastName();
    }

    @JsonProperty("user_id")
    public long getUserId() {
        return user.getId();
    }

}
