package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moodysalem.hibernate.model.BaseEntity;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;

@Entity
public class Application extends BaseEntity {
    @NotBlank
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "ownerId", updatable = false)
    private User owner;

    @Email
    @NotBlank
    @Column(name = "supportEmail")
    private String supportEmail;

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

    @URL
    @Lob
    @Column(name = "legacyUrl")
    private String legacyUrl;

    @URL
    @Lob
    @Column(name = "stylesheetUrl")
    private String stylesheetUrl;

    @URL
    @Lob
    @Column(name = "logoUrl")
    private String logoUrl;

    @Lob
    @Column(name = "description")
    private String description;


    @Column(name = "publicClientRegistration")
    private boolean publicClientRegistration;

    @JsonIgnore
    @Column(name = "deleted")
    private boolean deleted;

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

    public String getLegacyUrl() {
        return legacyUrl;
    }

    public void setLegacyUrl(String legacyUrl) {
        this.legacyUrl = legacyUrl;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isPublicClientRegistration() {
        return publicClientRegistration;
    }

    public void setPublicClientRegistration(boolean publicClientRegistration) {
        this.publicClientRegistration = publicClientRegistration;
    }

    public String getStylesheetUrl() {
        return stylesheetUrl;
    }

    public void setStylesheetUrl(String stylesheetUrl) {
        this.stylesheetUrl = stylesheetUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
