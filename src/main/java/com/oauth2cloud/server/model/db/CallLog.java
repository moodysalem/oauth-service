package com.oauth2cloud.server.model.db;

import com.moodysalem.hibernate.model.BaseEntity;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "call_log")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class CallLog extends BaseEntity {
    @NotEmpty
    @Column(name = "path", updatable = false)
    private String path;

    @Column(name = "ip", updatable = false)
    private String ip;

    @NotEmpty
    @Column(name = "method", updatable = false)
    private String method;

    @NotNull
    @Column(name = "timestamp", updatable = false)
    private Long timestamp;

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

    public Date getTimestamp() {
        return timestamp != null ? new Date(timestamp) : null;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp != null ? timestamp.getTime() : null;
    }

    @PrePersist
    public void setTimestamp() {
        if (getTimestamp() == null) {
            setTimestamp(new Date());
        }
    }
}
