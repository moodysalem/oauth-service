package com.oauth2cloud.server.applications.oauth.resources;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.oauth2cloud.server.applications.oauth.models.ResetPasswordModel;
import com.oauth2cloud.server.applications.oauth.models.UserCodeEmailModel;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.User;
import com.oauth2cloud.server.hibernate.model.UserCode;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Level;

@Path("reset")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class PasswordResetResource extends BaseResource {

    public static final String INVALID_RESET_PASSWORD_URL = "Invalid reset password URL.";
    public static final String INVALID_CODE_PLEASE_REQUEST_ANOTHER_RESET_PASSWORD_E_MAIL =
        "Invalid or expired link. Please request another reset password e-mail.";
    public static final String PASSWORD_IS_REQUIRED_AND_WAS_NOT_INCLUDED = "Password is required and was not included.";
    public static final String AN_INTERNAL_SERVER_ERROR_PREVENTED_YOU_FROM_CHANGING_YOUR_PASSWORD =
        "An internal server error prevented you from changing your password.";

    @QueryParam("applicationId")
    Long applicationId;

    @QueryParam("code")
    String code;

    @QueryParam("referer")
    String referer;

    private Application getApplication() {
        if (applicationId == null) {
            return null;
        }
        Application application = em.find(Application.class, applicationId);
        if (application != null && application.isDeleted()) {
            return null;
        }
        return application;
    }


    @GET
    @CORSFilter.Skip
    public Response sendEmailPage(@QueryParam("code") String code) {
        ResetPasswordModel rm = new ResetPasswordModel();

        if (code != null) {
            UserCode pcode = getCode(code, UserCode.Type.RESET, false);
            if (pcode != null) {
                rm.setUserCode(pcode);
                return Response.ok(new Viewable("/templates/ChangePassword", rm)).build();
            } else {
                return error(INVALID_CODE_PLEASE_REQUEST_ANOTHER_RESET_PASSWORD_E_MAIL);
            }
        } else {
            Application application = getApplication();
            if (application == null) {
                return error(INVALID_RESET_PASSWORD_URL);
            }
            logCall(application);
            rm.setApplication(application);
        }

        rm.setReferer(referer);

        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    @POST
    @CORSFilter.Skip
    public Response doPost(@FormParam("code") String code, @FormParam("password") String password, @FormParam("email") String email) {
        ResetPasswordModel rm = new ResetPasswordModel();

        if (code != null) {
            UserCode pc = getCode(code, UserCode.Type.RESET, false);
            if (pc == null) {
                return error(INVALID_CODE_PLEASE_REQUEST_ANOTHER_RESET_PASSWORD_E_MAIL);
            }
            if (password == null) {
                return error(PASSWORD_IS_REQUIRED_AND_WAS_NOT_INCLUDED);
            }
            pc.getUser().setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            pc.setUsed(true);
            try {
                beginTransaction();
                em.merge(pc.getUser());
                em.merge(pc);
                commit();
            } catch (Exception e) {
                rollback();
                LOG.log(Level.SEVERE, "Failed to change user password", e);
                return error(AN_INTERNAL_SERVER_ERROR_PREVENTED_YOU_FROM_CHANGING_YOUR_PASSWORD);
            }

            rm.setUserCode(pc);
            return Response.ok(new Viewable("/templates/ChangePassword", rm)).build();
        }

        Application application = getApplication();
        if (application == null) {
            return error(INVALID_RESET_PASSWORD_URL);
        }
        logCall(application);

        rm.setApplication(application);

        User u = getUser(email, application);
        if (u != null) {
            UserCode pc = makeCode(u, referer, UserCode.Type.RESET, new Date(System.currentTimeMillis() + FIVE_MINUTES));
            // do the e-mail
            emailCode(pc);
        }

        // always show success
        rm.setSuccess(true);
        rm.setReferer(referer);
        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    private void emailCode(UserCode pc) {
        String url = containerRequestContext.getUriInfo().getBaseUriBuilder().path("reset")
            .replaceQuery("").queryParam("code", pc.getCode()).toString();
        UserCodeEmailModel prem = new UserCodeEmailModel();
        prem.setUserCode(pc);
        prem.setUrl(url);
        sendEmail(pc.getUser().getApplication().getSupportEmail(), pc.getUser().getEmail(),
            "Your password reset code from " + pc.getUser().getApplication().getName(),
            "PasswordReset.ftl", prem);
    }


}
