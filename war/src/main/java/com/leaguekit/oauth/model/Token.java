package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Token extends BaseEntity {

    public enum Type {
        LOGIN, PERMISSION
    }

    @Column(name = "token")
    private String token;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "expires")
    private Date expires;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
