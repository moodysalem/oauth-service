package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;
import com.leaguekit.oauth.model.Client;
import com.leaguekit.oauth.model.Token;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Path("token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TokenResource extends BaseResource {

    private static final String BASIC = "Basic ";
    private static final int BASIC_LENGTH = BASIC.length();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String AUTHORIZATION_CODE = "authorization_code";

    @HeaderParam("Authorization")
    private String authorizationHeader;
    private boolean includedAuthentication;

    private Client client = null;

    @PostConstruct
    public void initClient() {
        if (authorizationHeader != null) {
            includedAuthentication = true;
            if (authorizationHeader.startsWith(BASIC)) {
                String credentials = authorizationHeader.substring(BASIC_LENGTH);
                String decoded = new String(Base64.getDecoder().decode(credentials.getBytes(UTF8)), UTF8);
                String[] pieces = decoded.split(":");
                if (pieces.length == 2) {
                    String clientId = pieces[0].trim();
                    String secret = pieces[1].trim();
                    Client tempClient = getClient(clientId);
                    if (secret.equals(tempClient.getSecret())) {
                        client = tempClient;
                    }
                }
            }
        }
    }

    @POST
    public Response post(
        MultivaluedMap<String, String> formParams
    ) {
        if (formParams == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid request body.");
        }

        String grantType = formParams.getFirst("grant_type");
        if (grantType == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "'grant_type' is required.");
        }

        switch (grantType) {
            case AUTHORIZATION_CODE:
                return authorizationCodeGrantType(formParams);
            default:
                throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid 'grant_type' specified.");
        }
    }

    private Response authorizationCodeGrantType(MultivaluedMap<String, String> formParams) {
        String code = formParams.getFirst("code");
        String redirectUri = formParams.getFirst("redirect_uri");
        String clientId = formParams.getFirst("client_id");

        if (code == null || redirectUri == null || clientId == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                "'code', 'redirect_uri', and 'client_id' are all required for the " + AUTHORIZATION_CODE + " grant type.");
        }

        Client c = getClient(clientId);
        if (c == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid client ID.");
        }

        if (client != null && !client.equals(c)) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Client authentication does not match client ID.");
        }

        if (c.getType().equals(Client.Type.CONFIDENTIAL) && client == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                "Client authentication is required for confidential clients.");
        }

        Token t = getToken(code, c, Token.Type.CODE);
        if (t == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid token.");
        }

        if (!redirectUri.equals(t.getRedirectUri())) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                "Redirect URI must exactly match the original redirect URI.");
        }

        // first expire the token
        t.setExpires(new Date());
        try {
            beginTransaction();
            em.merge(t);
            em.flush();
            commit();
        } catch (Exception e) {
            rollback();
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to expire the token.");
        }

        Token refreshToken = null;
        // we know the token is valid, so we should generate an access token now
        if (c.getRefreshTokenTtl() != null) {
            refreshToken = generateToken(Token.Type.REFRESH, c, t.getUser(), getExpires(c, true), redirectUri, new ArrayList<>(t.getAcceptedScopes()), null);
        }
        Token accessToken = generateToken(Token.Type.ACCESS, c, t.getUser(), getExpires(c, false), redirectUri, new ArrayList<>(t.getAcceptedScopes()), refreshToken);

        return Response.ok(accessToken).build();
    }

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

        Token t = getToken(token, client, Token.Type.ACCESS, Token.Type.REFRESH);
        if (t == null) {
            throw new RequestProcessingException(Response.Status.NOT_FOUND, "Token not found or expired.");
        }

        return Response.ok(t).build();
    }

}
