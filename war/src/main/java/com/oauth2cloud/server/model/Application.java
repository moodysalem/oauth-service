package com.oauth2cloud.server.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Application extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "facebookAppId")
    private Long facebookAppId;

    @Column(name = "facebookAppSecret")
    private String facebookAppSecret;

    @Column(name = "googleClientId")
    private String googleClientId;

    @Column(name = "googleClientSecret")
    private String googleClientSecret;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(Long facebookAppId) {
        this.facebookAppId = facebookAppId;
    }

    public String getFacebookAppSecret() {
        return facebookAppSecret;
    }

    public void setFacebookAppSecret(String facebookAppSecret) {
        this.facebookAppSecret = facebookAppSecret;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }
}
