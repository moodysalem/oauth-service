package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusResponse {
    private final long databaseVersion;
    private final String version;

    public StatusResponse(
            @JsonProperty("databaseVersion") long databaseVersion,
            @JsonProperty("version") String version
    ) {
        this.databaseVersion = databaseVersion;
        this.version = version;
    }

    public long getDatabaseVersion() {
        return databaseVersion;
    }

    public String getVersion() {
        return version;
    }
}
