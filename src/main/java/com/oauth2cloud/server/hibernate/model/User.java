package com.oauth2cloud.server.hibernate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

@Entity
public class User extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "applicationId", updatable = false)
    private Application application;

    @Email
    @NotEmpty
    @Column(name = "email", updatable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Transient
    private String newPassword;

    @NotEmpty
    @Column(name = "firstName")
    private String firstName;

    @NotEmpty
    @Column(name = "lastName")
    private String lastName;

    @Column(name = "verified")
    private boolean verified;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
