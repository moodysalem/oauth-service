package com.oauth2cloud.server.resources;

import com.oauth2cloud.server.model.Application;
import com.oauth2cloud.server.model.PasswordResetCode;
import com.oauth2cloud.server.model.User;
import com.leaguekit.util.RandomStringUtil;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

@Path("reset")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class PasswordResetResource extends BaseResource {

    public static final String INVALID_RESET_PASSWORD_URL = "Invalid reset password URL.";
    public static final String INVALID_CODE_PLEASE_REQUEST_ANOTHER_RESET_PASSWORD_E_MAIL = "Invalid or expired link. Please request another reset password e-mail.";
    public static final String PASSWORD_IS_REQUIRED_AND_WAS_NOT_INCLUDED = "Password is required and was not included.";
    public static final String AN_INTERNAL_SERVER_ERROR_PREVENTED_YOU_FROM_CHANGING_YOUR_PASSWORD = "An internal server error prevented you from changing your password.";

    public static class ResetPasswordModel {
        private Application application;
        private String error;
        private PasswordResetCode passwordResetCode;
        private boolean success;

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

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public PasswordResetCode getPasswordResetCode() {
            return passwordResetCode;
        }

        public void setPasswordResetCode(PasswordResetCode passwordResetCode) {
            this.passwordResetCode = passwordResetCode;
        }
    }

    @QueryParam("applicationId")
    Long applicationId;
    @QueryParam("code")
    String code;

    private Application getApplication() {
        if (applicationId == null) {
            return null;
        }
        return em.find(Application.class, applicationId);
    }


    private PasswordResetCode getCode(String code) {
        if (code == null) {
            return null;
        }
        CriteriaQuery<PasswordResetCode> pw = cb.createQuery(PasswordResetCode.class);
        Root<PasswordResetCode> rp = pw.from(PasswordResetCode.class);
        pw.select(rp).where(
            cb.equal(rp.get("code"), code),
            cb.greaterThan(rp.<Date>get("expires"), new Date()),
            cb.equal(rp.get("used"), false)
        );
        List<PasswordResetCode> lp = em.createQuery(pw).getResultList();
        return lp.size() == 1 ? lp.get(0) : null;
    }

    private PasswordResetCode makeCode(User user) {
        PasswordResetCode pw = new PasswordResetCode();
        pw.setExpires(new Date(System.currentTimeMillis() + FIVE_MINUTES));
        pw.setUser(user);
        pw.setCode(RandomStringUtil.randomAlphaNumeric(64));
        try {
            beginTransaction();
            em.persist(pw);
            commit();
        } catch (Exception e) {
            rollback();
            pw = null;
            LOG.log(Level.SEVERE, "Failed to create password reset code", e);
        }
        return pw;
    }


    @GET
    public Response sendEmailPage(@QueryParam("code") String code) {
        ResetPasswordModel rm = new ResetPasswordModel();

        if (code != null) {
            PasswordResetCode pcode = getCode(code);
            if (pcode != null) {
                rm.setPasswordResetCode(pcode);
                return Response.ok(new Viewable("/templates/ChangePassword", rm)).build();
            } else {
                return error(INVALID_CODE_PLEASE_REQUEST_ANOTHER_RESET_PASSWORD_E_MAIL);
            }
        } else {
            Application application = getApplication();
            if (application == null) {
                return error(INVALID_RESET_PASSWORD_URL);
            }
            rm.setApplication(application);
        }

        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    @POST
    public Response doPost(@FormParam("code") String code, @FormParam("password") String password, @FormParam("email") String email) {
        ResetPasswordModel rm = new ResetPasswordModel();

        if (code != null) {
            PasswordResetCode pc = getCode(code);
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

            rm.setPasswordResetCode(pc);
            return Response.ok(new Viewable("/templates/ChangePassword", rm)).build();
        }

        Application application = getApplication();
        if (application == null) {
            return error(INVALID_RESET_PASSWORD_URL);
        }

        rm.setApplication(application);

        User u = getUser(email, application);
        if (u != null) {
            PasswordResetCode pc = makeCode(u);
            // do the e-mail
            emailCode(pc);
        }

        // always show success
        rm.setSuccess(true);
        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    public static class EmailModel {
        private PasswordResetCode passwordResetCode;
        private String url;

        public PasswordResetCode getPasswordResetCode() {
            return passwordResetCode;
        }

        public void setPasswordResetCode(PasswordResetCode passwordResetCode) {
            this.passwordResetCode = passwordResetCode;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    private void emailCode(PasswordResetCode pc) {
        String url = containerRequestContext.getUriInfo().getRequestUriBuilder().replacePath("reset")
            .replaceQuery("").queryParam("code", pc.getCode()).toString();
        EmailModel em = new EmailModel();
        em.setPasswordResetCode(pc);
        em.setUrl(url);
        sendEmail(pc.getUser().getEmail(), "Your password reset code from " + pc.getUser().getApplication().getName(),
            "PasswordReset.ftl", em);
    }


}
