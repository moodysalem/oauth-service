package com.oauth2cloud.server.model.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.moodysalem.hibernate.model.BaseEntity;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "user_groups")
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class UserGroup extends BaseEntity {
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "group")
    private Set<User> users;

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
