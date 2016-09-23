package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.model.db.UserCode;
import com.oauth2cloud.server.hibernate.util.OldQueryHelper;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.model.data.ResetPasswordModel;
import com.oauth2cloud.server.model.data.UserCodeEmailModel;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

@NoXFrameOptionsFeature.NoXFrame
@Path(OAuth2Application.OAUTH + "/reset")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class PasswordResetResource extends OAuthResource {

    public static final String INVALID_RESET_PASSWORD_URL = "Invalid reset password URL.";
    public static final String INVALID_CODE_PLEASE_REQUEST_ANOTHER_RESET_PASSWORD_E_MAIL =
            "Invalid or expired link. Please request another reset password e-mail.";
    public static final String PASSWORD_IS_REQUIRED_AND_WAS_NOT_INCLUDED = "Password is required and was not included.";
    public static final String AN_INTERNAL_SERVER_ERROR_PREVENTED_YOU_FROM_CHANGING_YOUR_PASSWORD =
            "An internal server error prevented you from changing your password.";

    @QueryParam("applicationId")
    UUID applicationId;

    @QueryParam("code")
    String code;

    @QueryParam("referrer")
    String referrer;

    private Application getApplication(UUID applicationId) {
        if (applicationId == null) {
            return null;
        }
        Application application = em.find(Application.class, applicationId);
        if (application != null && application.isActive()) {
            return application;
        }
        return null;
    }


    @GET
    @CORSFilter.Skip
    public Response sendEmailPage(@QueryParam("code") String code) {
        ResetPasswordModel rm = new ResetPasswordModel();

        if (code != null) {
            UserCode pcode = OldQueryHelper.getUserCode(em, code, UserCode.Type.RESET, false);
            if (pcode != null) {
                rm.setUserCode(pcode);
                return Response.ok(new Viewable("/templates/ChangePassword", rm)).build();
            } else {
                return error(INVALID_CODE_PLEASE_REQUEST_ANOTHER_RESET_PASSWORD_E_MAIL);
            }
        } else {
            Application application = getApplication(applicationId);
            if (application == null) {
                return error(INVALID_RESET_PASSWORD_URL);
            }
            OldQueryHelper.logCall(em, application, containerRequestContext);
            rm.setApplication(application);
        }

        rm.setReferrer(referrer);

        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    @POST
    @CORSFilter.Skip
    public Response doPost(@FormParam("code") String code, @FormParam("password") String password, @FormParam("email") String email) {
        ResetPasswordModel rm = new ResetPasswordModel();

        if (code != null) {
            UserCode pc = OldQueryHelper.getUserCode(em, code, UserCode.Type.RESET, false);
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

        Application application = getApplication(applicationId);
        if (application == null) {
            return error(INVALID_RESET_PASSWORD_URL);
        }
        OldQueryHelper.logCall(em, application, containerRequestContext);

        rm.setApplication(application);

        User user = OldQueryHelper.getUser(em, email, application);
        if (user != null) {
            UserCode pc = OldQueryHelper.makeUserCode(em, user, referrer, UserCode.Type.RESET, new Date(System.currentTimeMillis() + FIVE_MINUTES));
            // do the e-mail
            emailCode(pc);
        }

        // always show success
        rm.setSuccess(true);
        rm.setReferrer(referrer);
        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    private void emailCode(UserCode pc) {
        String url = containerRequestContext.getUriInfo().getBaseUriBuilder()
                .path("oauth").path("reset").replaceQuery("").queryParam("code", pc.getCode()).toString();
        UserCodeEmailModel prem = new UserCodeEmailModel(userCode, url);
        prem.setUserCode(pc);
        prem.setUrl(url);
        sendEmail(pc.getUser().getApplication().getSupportEmail(), pc.getUser().getEmail(),
                "Your password reset code from " + pc.getUser().getApplication().getName(),
                "PasswordReset.ftl", prem);
    }


}
