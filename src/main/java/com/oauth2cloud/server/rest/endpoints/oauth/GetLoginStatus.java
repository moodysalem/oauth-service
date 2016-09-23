package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.oauth2cloud.server.hibernate.util.OldQueryHelper;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.data.LoginStatusModel;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;
import com.oauth2cloud.server.model.db.Token;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.AuthorizationHeaderTokenFeature;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Date;
import java.util.List;

@Produces(MediaType.TEXT_HTML)
@Path(OAuth2Application.OAUTH + "/loginstatus")
public class GetLoginStatus extends OAuthResource {

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
    @AuthorizationHeaderTokenFeature.ReadToken
    public Response get(@QueryParam("client_id") String clientId,
                        @HeaderParam("Referer") String referrer) {
        if (StringUtils.isBlank(clientId)) {
            return error("'client_id' parameter is required to check login status.");
        }

        final Client client = OldQueryHelper.getClient(em, clientId);

        if (client == null) {
            return error("Invalid client ID.");
        }

        OldQueryHelper.logCall(em, client, containerRequestContext);

        if (referrer == null) {
            return error("This page must be accessed from inside an iframe.");
        }

        boolean validReferrer = false;
        String referrerOrigin = null;
        try {
            URI u = new URI(referrer);
            for (String uri : client.getUris()) {
                try {
                    URI u2 = new URI(uri);
                    if (partialMatch(u2, u)) {
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
            return error("Invalid referrer.");
        }

        LoginCookie lc = getLoginCookie(client);

        LoginStatusModel lsm = new LoginStatusModel();
        lsm.setTargetOrigin(referrerOrigin);
        lsm.setLoginCookie(lc);

        if (lc != null) {
            List<Token> tokens = getUserTokens(client, lc.getUser());

            tokens.sort((a, b) -> b.getExpires().compareTo(a.getExpires()));

            if (tokens.size() > 0) {
                lsm.setTokenResponse(TokenResponse.from(tokens.get(0)));
            } else {
                // generate a token
                lsm.setTokenResponse(
                        TokenResponse.from(
                                OldQueryHelper.generateToken(
                                        em,
                                        Token.Type.ACCESS,
                                        client,
                                        lc.getUser(),
                                        getExpires(client, Token.Type.ACCESS),
                                        referrer,
                                        OldQueryHelper.getAcceptedScopes(em, client, lc.getUser()),
                                        null,
                                        null,
                                        null,
                                        null
                                )
                        )
                );
            }
        }

        return Response.ok(new Viewable("/templates/LoginStatus", lsm)).build();
    }

    List<Token> getUserTokens(Client client, User user) {
        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> tkn = tq.from(Token.class);

        tq.select(tkn).where(
                cb.equal(tkn.get(Token_.client), client),
                cb.equal(tkn.get(Token_.user), user),
                cb.equal(tkn.get(Token_.type), Token.Type.ACCESS),
                cb.greaterThan(tkn.get(Token_.expires), new Date())
        );

        return em.createQuery(tq).getResultList();
    }

}
