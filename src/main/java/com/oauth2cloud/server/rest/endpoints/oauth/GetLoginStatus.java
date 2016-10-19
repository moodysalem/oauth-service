package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.data.LoginStatusModel;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;
import com.oauth2cloud.server.model.db.UserAccessToken;
import com.oauth2cloud.server.rest.filter.TokenFilter;
import com.oauth2cloud.server.rest.util.CallLogUtil;
import com.oauth2cloud.server.rest.util.CookieUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;
import com.oauth2cloud.server.rest.util.UriUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Level;

import static com.oauth2cloud.server.rest.util.OAuthUtil.badRequest;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Api("oauth2")
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

    @ApiOperation(
            value = "Get Login Status",
            notes = "This endpoint allows you to retrieve a new token using a hidden iframe if the user is logged in to the application (via remember me feature using cookies)"
    )
    @GET
    @CORSFilter.Skip
    @TokenFilter.ReadToken
    public Response get(
            @ApiParam(value = "The ID of the client attempting to retrieve login status", required = true)
            @QueryParam("client_id") final String clientId,
            @ApiParam(value = "The referrer of the request-note the incorrect spelling", required = true)
            @HeaderParam("Referer") final String referrer
    ) {
        if (isBlank(clientId)) {
            return badRequest("'client_id' parameter is required to check login status.");
        }

        final Client client = QueryUtil.getClient(em, clientId);

        if (client == null) {
            return badRequest("Invalid client ID.");
        }

        CallLogUtil.logCall(em, client, req);

        if (referrer == null) {
            return badRequest(client, "This page must be accessed from inside an iframe.");
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
            return badRequest(client, "Invalid referrer.");
        }

        final LoginCookie loginCookie = CookieUtil.getLoginCookie(em, req, client);

        final TokenResponse newToken;
        if (loginCookie != null) {
            final UserAccessToken userAccessToken = new UserAccessToken();
            userAccessToken.setClient(client);
            userAccessToken.setUser(loginCookie.getUser());
            userAccessToken.setRedirectUri(referrer);
            userAccessToken.setAcceptedScopes(QueryUtil.getAcceptedScopes(em, client, loginCookie.getUser()));

            final UserAccessToken savedToken;
            try {
                savedToken = TXHelper.withinTransaction(em, () -> em.merge(userAccessToken));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to create token from cookie");
                return badRequest(client, "An internal server error occurred!");
            }
            newToken = TokenResponse.from(savedToken);
        } else {
            newToken = null;
        }

        return Response.ok(
                new Viewable("/templates/LoginStatus", new LoginStatusModel(loginCookie, referrerOrigin, newToken))
        ).build();
    }


}
