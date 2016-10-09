package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.model.api.LoginErrorCode;
import com.oauth2cloud.server.model.db.ClientScope;
import com.oauth2cloud.server.model.db.LoginCode;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.util.OAuthUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Date;
import java.util.Set;

@Path("login/{code}")
public class LoginResource extends BaseResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    @NoXFrameOptionsFeature.NoXFrame
    public Response login(@PathParam("code") final String code) {
        final LoginCode loginCode = QueryUtil.findLoginCode(em, code);

        if (loginCode == null) {
            return OAuthUtil.badRequest("Invalid login code!");
        }

        if (loginCode.getUsed()) {
            return backToLogin(loginCode, LoginErrorCode.login_code_used);
        }

        if (loginCode.getExpires().after(new Date())) {
            return backToLogin(loginCode, LoginErrorCode.login_code_expired);
        }

        final Set<ClientScope> scopes =
                QueryUtil.getScopesToRequest(em, loginCode.getClient(), loginCode.getUser(), OAuthUtil.getScopes(loginCode.getScope()));

        return Response.ok().build();
    }

    private Response backToLogin(final LoginCode loginCode, final LoginErrorCode loginErrorCode) {
        return Response.temporaryRedirect(
                UriBuilder.fromUri(loginCode.getBaseUri()).path("authorize")
                        .queryParam("client_id", loginCode.getClient().getId())
                        .queryParam("response_type", loginCode.getResponseType())
                        .queryParam("redirect_uri", loginCode.getRedirectUri())
                        .queryParam("scope", loginCode.getScope())
                        .queryParam("state", loginCode.getState())
                        .queryParam("error_code", loginErrorCode.name())
                        .build()
        ).build();
    }
}
