package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * A login code is created when a user attempts to log in and an e-mail is sent
 */
@Entity
@Immutable
@Table(name = "login_code")
public class LoginCode extends BaseEntity {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id", updatable = false)
    private Client client;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @NotNull
    @Column(name = "expires", updatable = false)
    private Long expires;

    @NotNull
    @Column(name = "code", updatable = false)
    private String code;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpires() {
        return expires == null ? null : new Date(expires);
    }

    public void setExpires(Date expires) {
        this.expires = expires == null ? null : expires.getTime();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
