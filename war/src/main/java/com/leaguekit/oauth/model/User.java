package com.leaguekit.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.*;

@Entity
public class User extends BaseEntity {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "applicationId")
    private Application application;

    @Column(name = "email")
    private String email;

    @JsonIgnore
    @Column(name = "password")
    private String password;

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
}
