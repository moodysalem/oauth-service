package com.oauth2cloud.server.hibernate.model;

import com.moodysalem.hibernate.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

// we use this to represent a logged in user
@Entity
public class LoginCookie extends BaseEntity {

    @Column(name = "secret")
    private String secret;

    @Column(name = "expires")
    private Date expires;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;


    @Column(name = "rememberMe")
    private boolean rememberMe;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
