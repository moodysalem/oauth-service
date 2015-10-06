package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;
import com.leaguekit.oauth.model.Client;
import com.leaguekit.oauth.model.Token;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TokenResource extends BaseResource {

    /**
     * Get the info for a token for a specific client
     *
     * @param token    unique string representing the token
     * @param clientId the client id for which the token was issued
     * @return the token information
     */
    @GET
    public Response get(@QueryParam("token") String token, @QueryParam("clientId") String clientId) {
        if (token == null || clientId == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "'token' and 'clientId' query parameters are required.");
        }

        Client client = getClient(clientId);
        if (client == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid client identifier.");
        }

        Token t = getToken(token, client);
        if (t == null) {
            throw new RequestProcessingException(Response.Status.NOT_FOUND, "Token not found or expired.");
        }

        return Response.ok(t).build();
    }

    /**
     * Get a token given the token string and the client it's for
     *
     * @param token  the token string
     * @param client the client it was issued to
     * @return the token or null if it doesn't exist or has expired
     */
    private Token getToken(String token, Client client) {
        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> t = tq.from(Token.class);
        tq.select(t).where(
            cb.and(
                cb.equal(t.get("token"), token),
                cb.or(
                    cb.equal(t.get("type"), Token.Type.ACCESS),
                    cb.equal(t.get("type"), Token.Type.REFRESH)
                ),
                cb.greaterThan(t.<Date>get("expires"), new Date()),
                cb.equal(t.get("client"), client)
            )
        );

        List<Token> tkns = em.createQuery(tq).getResultList();
        return tkns.size() == 1 ? tkns.get(0) : null;
    }

}
