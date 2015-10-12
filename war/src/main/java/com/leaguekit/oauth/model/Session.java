package com.leaguekit.oauth.model;

import com.leaguekit.hibernate.model.BaseEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

// we use the DB to store http sessions
@Entity
public class Session extends BaseEntity {

    @Column(name = "secret")
    private String secret;

    @Column(name = "expires")
    private Date expires;

    @ManyToMany
    @JoinTable(
        name = "Session_User",
        joinColumns = @JoinColumn(name = "sessionId"),
        inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private Set<User> users;

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

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
