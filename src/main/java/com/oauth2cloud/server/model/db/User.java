package com.oauth2cloud.server.model.db;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users")
public class User extends VersionedEntity {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "application_id", updatable = false)
    private Application application;

    @Email
    @NotEmpty
    @Column(name = "email", updatable = false)
    private String email;

    @ManyToOne
    @JoinColumn(name = "user_group_id")
    private UserGroup group;

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

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }
}
