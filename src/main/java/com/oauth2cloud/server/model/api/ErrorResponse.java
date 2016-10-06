package com.oauth2cloud.server.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.hibernate.util.QueryUtil;
import com.oauth2cloud.server.rest.util.QueryString;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URLEncoder;
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
    private final Type error;
    @JsonProperty("error_description")
    private final String errorDescription;
    @JsonProperty("error_uri")
    private final String errorUri;

    public ErrorResponse(Type error, String errorDescription, String errorUri) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.errorUri = errorUri;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public Type getError() {
        return error;
    }

    @Override
    public String toString() {
        final MultivaluedMap<String, String> toConcatenate = new MultivaluedHashMap<>();

        if (getError() != null) {
            toConcatenate.putSingle("error", getError().name());
        }
        if (getErrorDescription() != null) {
            toConcatenate.putSingle("error_description", getErrorDescription());
        }
        if (getErrorUri() != null) {
            toConcatenate.putSingle("error_uri", getErrorUri());
        }

        return QueryString.mapToQueryString(toConcatenate);
    }
}
