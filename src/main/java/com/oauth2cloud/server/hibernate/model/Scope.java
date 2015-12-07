package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moodysalem.hibernate.model.BaseEntity;
import com.oauth2cloud.server.hibernate.validate.NoSpaces;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Scope extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "applicationId")
    private Application application;

    @NoSpaces
    @NotBlank
    @Column(name = "name")
    private String name;

    @NotBlank
    @Column(name = "displayName")
    private String displayName;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "description")
    private String description;

    @Column(name = "requiresApprovalFromApplication")
    private boolean requiresApprovalFromApplication;

    @JsonIgnore
    @Column(name = "deleted")
    private boolean deleted;

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

    public boolean isRequiresApprovalFromApplication() {
        return requiresApprovalFromApplication;
    }

    public void setRequiresApprovalFromApplication(boolean requiresApprovalFromApplication) {
        this.requiresApprovalFromApplication = requiresApprovalFromApplication;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
