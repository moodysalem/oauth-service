package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * A simple wrapper around a user that is communicated to clients
 */
public class UserDetails {

    public UserDetails() {
    }

    public static UserDetails from(User u) {
        if (u == null) {
            throw new NullPointerException("Null user passed to UserDetails constructor.");
        }

        UserDetails ud = new UserDetails();
        ud.setEmail(u.getEmail());
        ud.setFirstName(u.getFirstName());
        ud.setLastName(u.getLastName());
        ud.setUserId(u.getId());

        return ud;
    }

    @JsonProperty("email")
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("user_id")
    private UUID userId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
