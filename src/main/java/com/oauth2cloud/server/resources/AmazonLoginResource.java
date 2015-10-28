package com.oauth2cloud.server.resources;

import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("amazon")
public class AmazonLoginResource extends BaseResource {

    public static class AmazonLoginResponse {
        private String code;
        private String errorDescription;
        private String error;


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public void setErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    @GET
    public Response getToken(
            @QueryParam("code") String code,
            @QueryParam("error") String error,
            @QueryParam("error_description") String errorDescription
    ) {
        AmazonLoginResponse ar = new AmazonLoginResponse();
        ar.setErrorDescription(errorDescription);
        ar.setError(error);
        ar.setCode(code);
        return Response.ok(new Viewable("/templates/AmazonLogin", ar)).build();
    }
}
