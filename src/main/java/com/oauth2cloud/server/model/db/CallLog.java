package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

@Table(name = "call_log")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class CallLog extends BaseEntity {
    @NotEmpty
    @Column(name = "ip", updatable = false)
    private String ip;

    @Lob
    @Column(name = "path", updatable = false)
    private String path;

    @NotEmpty
    @Column(name = "method", updatable = false)
    private String method;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
