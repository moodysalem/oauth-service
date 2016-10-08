package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.data.LoginStatusModel;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;
import com.oauth2cloud.server.model.db.Token;
import com.oauth2cloud.server.model.db.TokenType;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import com.oauth2cloud.server.rest.util.CallLogUtil;
import com.oauth2cloud.server.rest.util.CookieUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;
import com.oauth2cloud.server.rest.util.UriUtil;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.oauth2cloud.server.model.db.Token.getExpires;
import static com.oauth2cloud.server.rest.util.OAuthUtil.badRequest;

@Produces(MediaType.TEXT_HTML)
@Path("login-status")
public class GetLoginStatus extends BaseResource {

    /**
     * This endpoint returns a page that uses HTML5 window.postMessage to send information to the
     * parent window about whether the user is logged in and the user's token if they indeed are
     * logged in.
     *
     * @param clientId id of the client checking login status
     * @return an html page that posts a message to the parent window
     */
    @GET
    @CORSFilter.Skip
    @TokenFilter.ReadToken
    public Response get(@QueryParam("client_id") String clientId,
                        @HeaderParam("Referer") String referrer) {
        if (StringUtils.isBlank(clientId)) {
            return badRequest("'client_id' parameter is required to check login status.");
        }

        final Client client = QueryUtil.getClient(em, clientId);

        if (client == null) {
            return badRequest("Invalid client ID.");
        }

        CallLogUtil.logCall(em, client, req);

        if (referrer == null) {
            return badRequest("This page must be accessed from inside an iframe.");
        }

        boolean validReferrer = false;
        String referrerOrigin = null;
        try {
            final URI u = new URI(referrer);
            for (final String uri : client.getUris()) {
                try {
                    final URI u2 = new URI(uri);
                    if (UriUtil.partialMatch(u2, u)) {
                        validReferrer = true;
                        // remove the path
                        referrerOrigin = UriBuilder
                                .fromUri(u2)
                                .replacePath("")
                                .replaceQuery("")
                                .build()
                                .toString();
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        if (!validReferrer) {
            return badRequest("Invalid referrer.");
        }

        final LoginCookie loginCookie = CookieUtil.getLoginCookie(em, req, client);

        TokenResponse tokenResponse = null;
        if (loginCookie != null) {
            final Token existing = QueryUtil.getNewestUserAccessToken(em, client, loginCookie.getUser());

            if (existing != null) {
                tokenResponse = TokenResponse.from(existing);
            } else {
                // generate a token
                tokenResponse = TokenResponse.from(
                        QueryUtil.generateToken(
                                em,
                                TokenType.ACCESS,
                                client,
                                loginCookie.getUser(),
                                getExpires(client, TokenType.ACCESS),
                                referrer,
                                QueryUtil.getAcceptedScopes(em, client, loginCookie.getUser()),
                                null,
                                null
                        )
                );
            }
        }

        return Response.ok(
                new Viewable("/templates/LoginStatus", new LoginStatusModel(loginCookie, referrerOrigin, tokenResponse))
        ).build();
    }


}
