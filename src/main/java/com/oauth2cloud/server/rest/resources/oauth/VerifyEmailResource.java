package com.oauth2cloud.server.rest.resources.oauth;

import com.oauth2cloud.server.hibernate.model.UserCode;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.models.VerifyEmailModel;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.logging.Level;

@NoXFrameOptionsFeature.NoXFrame
@Path(OAuth2Application.OAUTH + "/verify")
public class VerifyEmailResource extends OAuthResource {

    public static final String INVALID_VERIFICATION_LINK = "Invalid verification link.";
    public static final String ALREADY_VERIFIED = "Your user is already verified.";
    public static final String VERIFICATION_CODE_ALREADY_USED_NOT_VERIFIED = "This verification code is already used but your user is not verified. Please contact an administrator.";
    public static final String YOUR_E_MAIL_HAS_BEEN_VERIFIED = "Your e-mail has been verified.";
    public static final String FAILED_TO_VERIFY_UNKNOWN_REASON = "Failed to verify your e-mail address for an unknown reason. Please contact an administrator.";

    @GET
    public Response verifyEmail(@QueryParam("code") String code) {
        if (code == null) {
            return error(INVALID_VERIFICATION_LINK);
        }

        UserCode uc = getCode(code, UserCode.Type.VERIFY, true);
        if (uc == null) {
            return error(INVALID_VERIFICATION_LINK);
        }
        logCall(uc.getUser().getApplication());

        VerifyEmailModel vem = new VerifyEmailModel();
        vem.setUserCode(uc);

        if (uc.getUser().isVerified()) {
            vem.setMessage(ALREADY_VERIFIED);
            vem.setAlertLevel(VerifyEmailModel.AlertLevel.warning);
        } else {
            if (uc.isUsed()) {
                vem.setMessage(VERIFICATION_CODE_ALREADY_USED_NOT_VERIFIED);
                vem.setAlertLevel(VerifyEmailModel.AlertLevel.danger);
            } else {
                // the common case, user is not verified, code isn't used
                try {
                    beginTransaction();
                    uc.setUsed(true);
                    em.merge(uc);
                    uc.getUser().setVerified(true);
                    em.merge(uc.getUser());
                    commit();
                    vem.setAlertLevel(VerifyEmailModel.AlertLevel.success);
                    vem.setMessage(YOUR_E_MAIL_HAS_BEEN_VERIFIED);
                } catch (Exception e) {
                    rollback();
                    LOG.log(Level.SEVERE, "Failed to verify user e-mail", e);
                    vem.setAlertLevel(VerifyEmailModel.AlertLevel.danger);
                    vem.setMessage(FAILED_TO_VERIFY_UNKNOWN_REASON);
                }
            }
        }

        return Response.ok(new Viewable("/templates/VerifyEmailPage", vem)).build();
    }

}
