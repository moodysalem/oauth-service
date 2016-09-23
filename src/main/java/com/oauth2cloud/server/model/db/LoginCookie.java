package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Date;

// we use this to represent a logged in user
@Entity
@Immutable
public class LoginCookie extends BaseEntity {
    @NotEmpty
    @Column(name = "secret")
    private String secret;

    @NotNull
    @Column(name = "expires")
    private Date expires;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "remember_me")
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
