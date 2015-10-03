package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Scope extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "applicationId")
    private Application application;

    @Column(name = "name")
    private String name;

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
}
