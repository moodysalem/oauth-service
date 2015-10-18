package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.Application;
import com.leaguekit.oauth.model.PasswordResetCode;
import com.leaguekit.oauth.model.User;
import com.leaguekit.util.RandomStringUtil;
import org.glassfish.jersey.server.mvc.Viewable;

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
public class ResetPasswordResource extends BaseResource {

    public static final String INVALID_RESET_PASSWORD_URL = "Invalid reset password URL.";
    private static final String FROM_EMAIL = "moody@leaguekit.com";

    public static class ResetPasswordModel {
        private Application application;
        private String error;
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


    private PasswordResetCode getCode() {
        if (code == null) {
            return null;
        }
        CriteriaQuery<PasswordResetCode> pw = cb.createQuery(PasswordResetCode.class);
        Root<PasswordResetCode> rp = pw.from(PasswordResetCode.class);
        pw.select(rp).where(cb.equal(rp.get("code"), code), cb.greaterThan(rp.<Date>get("expires"), new Date()));
        List<PasswordResetCode> lp = em.createQuery(pw).getResultList();
        return lp.size() == 1 ? lp.get(0) : null;
    }

    private User getUser(String email) {
        CriteriaQuery<User> uq = cb.createQuery(User.class);
        Root<User> ru = uq.from(User.class);
        uq.select(ru).where(cb.equal(ru.get("email"), email));
        List<User> lu = em.createQuery(uq).getResultList();
        return lu.size() == 1 ? lu.get(0) : null;
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
    public Response sendEmailPage(@QueryParam("token") String token) {
        Application application = getApplication();
        if (application == null) {
            return error(INVALID_RESET_PASSWORD_URL);
        }

        ResetPasswordModel rm = new ResetPasswordModel();
        rm.setApplication(application);

        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    @POST
    public Response doSendEmail(@FormParam("email") String email) {
        Application application = getApplication();
        if (application == null) {
            return error(INVALID_RESET_PASSWORD_URL);
        }
        ResetPasswordModel rm = new ResetPasswordModel();
        rm.setApplication(application);

        User u = getUser(email);
        if (u != null) {
            PasswordResetCode pc = makeCode(u);
            // do the e-mail
            emailCode(pc);
        }

        // always show success
        rm.setSuccess(true);
        return Response.ok(new Viewable("/templates/ResetPassword", rm)).build();
    }

    private void emailCode(PasswordResetCode pc) {
        sendEmail(FROM_EMAIL, pc.getUser().getEmail(), "Your password reset code from " + pc.getUser().getApplication().getName(),
            "PasswordReset.ftl", pc);
    }


}
