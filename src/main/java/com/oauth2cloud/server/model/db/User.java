package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.VersionedEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Immutable
public class User extends VersionedEntity {
    @ManyToOne
    @JoinColumn(name = "application_id", updatable = false)
    private Application application;

    @Email
    @NotEmpty
    @Column(name = "email", updatable = false)
    private String email;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
