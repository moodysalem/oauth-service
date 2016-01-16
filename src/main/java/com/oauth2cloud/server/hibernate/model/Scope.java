package com.oauth2cloud.server.hibernate.model;

import com.moodysalem.hibernate.model.BaseEntity;
import com.oauth2cloud.server.hibernate.validate.NoSpaces;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;

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

    @Lob
    @Column(name = "thumbnail")
    private String thumbnail;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "requiresApprovalFromApplication")
    private boolean requiresApprovalFromApplication;

    @Column(name = "active")
    private boolean active;

    @Column(name = "requestable")
    private boolean requestable;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRequestable() {
        return requestable;
    }

    public void setRequestable(boolean requestable) {
        this.requestable = requestable;
    }
}
