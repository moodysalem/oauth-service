package com.oauth2cloud.server.model.db;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "application_call_log")
public class ApplicationCallLog extends CallLog {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "application_id", updatable = false)
    private Application application;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
