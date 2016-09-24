package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

@Entity
@Immutable
public class CallLog extends BaseEntity {
    @NotEmpty
    @Column(name = "ip", updatable = false)
    private String ip;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Lob
    @Column(name = "path")
    private String path;

    @Column(name = "method")
    private String method;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}