package com.leaguekit.oauth.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.leaguekit.oauth.exceptions.RequestProcessingException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;

@Provider
public class RequestProcessingExceptionMapper implements ExceptionMapper<RequestProcessingException> {


    private static final ObjectMapper om = new ObjectMapper();

    public static final String MESSAGE_KEY = "message";
    public static final String NUMBER_OF_ERRORS_HEADER = "X-Number-Of-Errors";

    @Override
    public Response toResponse(RequestProcessingException e) {

        ArrayNode errors = om.createArrayNode();

        List<String> errStrings = e.getErrors();
        if (errStrings != null) {
            for (String err : errStrings) {
                if (err != null) {
                    ObjectNode errObj = om.createObjectNode();
                    errObj.put(MESSAGE_KEY, err);
                    errors.add(errObj);
                }
            }
        }

        return Response.status(e.getStatusCode())
            .entity(errors)
            .header(NUMBER_OF_ERRORS_HEADER, errors.size())
            .build();
    }
}
