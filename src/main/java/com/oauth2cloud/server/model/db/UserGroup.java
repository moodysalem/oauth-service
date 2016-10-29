package com.oauth2cloud.server.model.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.moodysalem.hibernate.model.BaseEntity;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "user_groups")
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class UserGroup extends BaseEntity {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "application_id", updatable = false)
    private Application application;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "group")
    private Set<User> users;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
