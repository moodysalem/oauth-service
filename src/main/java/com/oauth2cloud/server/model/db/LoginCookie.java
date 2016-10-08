package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "login_cookies")
public class LoginCookie extends BaseEntity {
    @NotEmpty
    @Column(name = "secret", updatable = false)
    private String secret;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @Column(name = "remember_me", updatable = false)
    private boolean rememberMe;

    @NotNull
    @Column(name = "expires")
    private Long expires;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Date getExpires() {
        return expires == null ? null : new Date(expires);
    }

    public void setExpires(Date expires) {
        this.expires = expires == null ? null : expires.getTime();
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
