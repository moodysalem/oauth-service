package com.oauth2cloud.server.rest.resources.oauth;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.oauth2cloud.server.hibernate.model.*;
import com.oauth2cloud.server.rest.OAuth2Cloud;
import com.oauth2cloud.server.rest.filter.TokenFeature;
import com.oauth2cloud.server.rest.models.LoginStatusModel;
import com.oauth2cloud.server.rest.resources.BaseResource;
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

@Path(OAuth2Cloud.OAUTH + "/loginstatus")
@Produces(MediaType.TEXT_HTML)
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
    @TokenFeature.ReadToken
    public Response get(@QueryParam("client_id") String clientId,
                        @HeaderParam("Referer") String referrer) {
        if (clientId == null) {
            return error("'client_id' parameter is required to check login status.");
        }

        Client c = getClient(clientId);

        if (c == null) {
            return error("Invalid client ID.");
        }

        logCall(c);

        if (referrer == null) {
            return error("This page must be accessed from inside an iframe.");
        }

        boolean validReferrer = false;
        String referrerOrigin = null;
        try {
            URI u = new URI(referrer);
            for (String uri : c.getUris()) {
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

        LoginCookie lc = getLoginCookie(c);

        LoginStatusModel lsm = new LoginStatusModel();
        lsm.setTargetOrigin(referrerOrigin);
        lsm.setLoginCookie(lc);

        if (lc != null) {
            List<Token> tokens = getUserTokens(c, lc.getUser());

            tokens.sort((a, b) -> b.getExpires().compareTo(a.getExpires()));

            if (tokens.size() > 0) {
                lsm.setTokenResponse(TokenResponse.from(tokens.get(0)));
            }
        }

        return Response.ok(new Viewable("/templates/LoginStatus", lsm)).build();
    }

    List<Token> getUserTokens(Client client, User user) {
        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> tkn = tq.from(Token.class);

        tq.select(tkn).where(
            cb.equal(tkn.get("client"), client),
            cb.equal(tkn.get("user"), user),
            cb.equal(tkn.get("type"), Token.Type.ACCESS),
            cb.greaterThan(tkn.get("expires"), new Date())
        );

        return em.createQuery(tq).getResultList();
    }

}
