package com.oauth2cloud.server.rest.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The OAauth2 spec for an error response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public enum Type {
        invalid_request, invalid_client, invalid_grant, unauthorized_client, unsupported_grant_type, invalid_scope, access_denied
    }

    @JsonProperty("error")
    private Type error;
    @JsonProperty("error_description")
    private String errorDescription;
    @JsonProperty("error_uri")
    private String errorUri;

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
    }

    public Type getError() {
        return error;
    }

    public void setError(Type error) {
        this.error = error;
    }

    @Override
    public String toString() {
        Map<String, String> toConcatenate = new HashMap<>();
        if (getError() != null) {
            toConcatenate.put("error", getError().name());
        }
        if (getErrorDescription() != null) {
            toConcatenate.put("error_description", getErrorDescription());
        }
        if (getErrorUri() != null) {
            toConcatenate.put("error_uri", getErrorUri());
        }
        return toConcatenate.keySet().stream().map((k) -> k + "=" + toConcatenate.get(k)).collect(Collectors.joining("&"));
    }
}
