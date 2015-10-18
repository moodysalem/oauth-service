package com.oauth2cloud.server.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Application extends BaseEntity {
    @Column(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
