package com.oauth2cloud.server.hibernate.model;

import com.leaguekit.hibernate.model.BaseEntity;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;

@Entity
public class Application extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "facebookAppId")
    private Long facebookAppId;

    @Column(name = "facebookAppSecret")
    @Convert(converter = EncryptedStringConverter.class)
    private String facebookAppSecret;

    @Column(name = "googleClientId")
    private String googleClientId;

    @Column(name = "googleClientSecret")
    @Convert(converter = EncryptedStringConverter.class)
    private String googleClientSecret;

    @Column(name = "amazonClientId")
    private String amazonClientId;

    @Column(name = "amazonClientSecret")
    @Convert(converter = EncryptedStringConverter.class)
    private String amazonClientSecret;

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

    public String getAmazonClientId() {
        return amazonClientId;
    }

    public void setAmazonClientId(String amazonClientId) {
        this.amazonClientId = amazonClientId;
    }

    public String getAmazonClientSecret() {
        return amazonClientSecret;
    }

    public void setAmazonClientSecret(String amazonClientSecret) {
        this.amazonClientSecret = amazonClientSecret;
    }
}
