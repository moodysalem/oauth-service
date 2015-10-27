package com.oauth2cloud.server.hibernate.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * These objects are created when we need something specific to a user in an application, but not specific to a client
 */
@Entity
public class UserCode extends BaseEntity {

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        VERIFY, RESET
    }

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "code")
    private String code;

    @Column(name = "expires")
    private Date expires;

    @Column(name = "used")
    private boolean used;

    @Lob
    @Column(name = "referer")
    private String referer;

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

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
