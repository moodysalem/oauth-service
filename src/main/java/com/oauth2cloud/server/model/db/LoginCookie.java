package com.oauth2cloud.server.model.db;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * A cookie represents that a user has logged in to the application
 */
@Entity
@Table(name = "login_cookies")
public class LoginCookie extends OAuthVersionedEntity {
    private static final long ONE_MONTH_MS = 1000L * 60L * 60L * 24L * 30L;

    @NotEmpty
    @Column(name = "secret", updatable = false)
    private String secret;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

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

    public void expiresInOneMonth() {
        setExpires(new Date(System.currentTimeMillis() + ONE_MONTH_MS));
    }

    @PrePersist
    public void setExpires() {
        if (getExpires() == null) {
            expiresInOneMonth();
        }
    }
}
