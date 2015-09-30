package com.leaguekit.oauth.model;

import javax.persistence.Entity;

//@Entity
public class Token extends BaseEntity{
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
