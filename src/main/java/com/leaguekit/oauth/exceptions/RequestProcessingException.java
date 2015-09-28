package com.leaguekit.oauth.exceptions;


import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RequestProcessingException extends RuntimeException {

    private List<String> errors = new ArrayList<>();
    private Response.Status statusCode = Response.Status.BAD_REQUEST;

    public List<String> getErrors() {
        return errors;
    }

    public Response.Status getStatusCode() {
        return statusCode;
    }

    // default to bad request
    public RequestProcessingException(String... errors) {
        this.errors.addAll(Arrays.asList(errors));
    }


    public RequestProcessingException(Response.Status statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public RequestProcessingException(Response.Status statusCode, String... errors) {
        this(statusCode);
        this.errors.addAll(Arrays.asList(errors));
    }

    public RequestProcessingException(Response.Status statusCode, Collection<String> errors) {
        this(statusCode);
        this.errors.addAll(errors);
    }

    public RequestProcessingException(Collection<String> errors) {
        this.errors.addAll(errors);
    }

    @Override
    public String getMessage() {
        return join(this.errors, "; ");
    }

    private static String join (List<String> strings, String separator) {
        if (strings == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String x : strings) {
            if (x == null) {
                continue;
            }
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(x);
        }
        return result.toString();
    }

}
