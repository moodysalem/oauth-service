package com.oauth2cloud.server.model.db;

import com.oauth2cloud.server.hibernate.validate.NoSpaces;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;

@Entity
@Table(name = "scopes")
public class Scope extends OAuthVersionedEntity {
    @ManyToOne
    @JoinColumn(name = "application_id", updatable = false)
    private Application application;

    @NoSpaces
    @NotBlank
    @Column(name = "name", updatable = false)
    private String name;

    @NotBlank
    @Column(name = "display_name")
    private String displayName;

    @Lob
    @Column(name = "thumbnail")
    private String thumbnail;

    @Lob
    @Column(name = "description")
    private String description;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
