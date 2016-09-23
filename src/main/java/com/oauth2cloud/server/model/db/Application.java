package com.oauth2cloud.server.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moodysalem.hibernate.model.VersionedEntity;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.Valid;

@Entity
public class Application extends VersionedEntity {
    @NotBlank
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "owner_id", updatable = false)
    private User owner;

    @Email
    @NotBlank
    @Column(name = "support_email")
    private String supportEmail;

    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "google_client_id")),
            @AttributeOverride(name = "secret", column = @Column(name = "google_client_secret"))
    })
    private ClientCredentials googleCredentials;

    @URL
    @Lob
    @Column(name = "stylesheet_url")
    private String stylesheetUrl;

    @URL
    @Lob
    @Column(name = "logo_url")
    private String logoUrl;

    @URL
    @Lob
    @Column(name = "favicon_url")
    private String faviconUrl;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientCredentials getGoogleCredentials() {
        return googleCredentials;
    }

    public void setGoogleCredentials(ClientCredentials googleCredentials) {
        this.googleCredentials = googleCredentials;
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

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
