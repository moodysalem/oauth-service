package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.Application;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("reset")
@Produces(MediaType.TEXT_HTML)
public class ResetPasswordResource extends BaseResource {

    public static final String INVALID_RESET_PASSWORD_URL = "Invalid reset password URL.";

    public static class ResetPasswordModel {
        private Application application;
        private String error;

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Application getApplication() {
            return application;
        }

        public void setApplication(Application application) {
            this.application = application;
        }
    }

    @QueryParam("applicationId")
    Long applicationId;

    @GET
    public Response sendEmail() {
        if (applicationId == null) {
            return error(INVALID_RESET_PASSWORD_URL);
        }
        Application application = em.find(Application.class, applicationId);
        if (application == null) {
            return error(INVALID_RESET_PASSWORD_URL);
        }

        ResetPasswordModel rm = new ResetPasswordModel();
        rm.setApplication(application);

        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }


}
