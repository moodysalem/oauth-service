package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.VersionedEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * These objects are created when we need something specific to a user in an application, but not specific to a client
 * such as a verification code or a reset code
 */
@Entity
public class UserCode extends VersionedEntity {
    public enum Type {
        VERIFY, RESET
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "code")
    private String code;

    @Column(name = "expires")
    private Date expires;

    @Column(name = "used")
    private boolean used;

    @Lob
    @Column(name = "referrer")
    private String referrer;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
}
