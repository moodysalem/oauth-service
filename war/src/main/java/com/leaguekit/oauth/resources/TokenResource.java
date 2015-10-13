package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;
import com.leaguekit.oauth.model.Client;
import com.leaguekit.oauth.model.Token;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

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

    private Client client = null;

    @PostConstruct
    public void initClient() {
        if (authorizationHeader != null) {
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
    public Response post(MultivaluedMap<String, String> formParams) {
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

        Client client = getClient(clientId);
        if (client == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid client ID.");
        }

        if (this.client != null && !this.client.equals(client)) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Client authentication does not match client ID.");
        }

        if (client.getType().equals(Client.Type.CONFIDENTIAL) && this.client == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                "Client authentication is required for confidential clients.");
        }

        Token codeToken = getToken(code, client, Token.Type.CODE);
        if (codeToken == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid token.");
        }

        if (!redirectUri.equals(codeToken.getRedirectUri())) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                "Redirect URI must exactly match the original redirect URI.");
        }

        // first expire the token
        expireToken(codeToken);

        Token refreshToken = null;
        // we know the token is valid, so we should generate an access token now
        // only confidential clients may receive refresh tokens
        if (client.getRefreshTokenTtl() != null && !client.getType().equals(Client.Type.CONFIDENTIAL)) {
            refreshToken = generateToken(Token.Type.REFRESH, client, codeToken.getUser(), getExpires(client, true), redirectUri,
                new ArrayList<>(codeToken.getAcceptedScopes()), null);
        }
        Token accessToken = generateToken(Token.Type.ACCESS, client, codeToken.getUser(), getExpires(client, false), redirectUri,
            new ArrayList<>(codeToken.getAcceptedScopes()), refreshToken);

        return Response.ok(accessToken).build();
    }

    private void expireToken(Token t) {
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
    }

    /**
     * Get the info for a token for a specific client
     *
     * @param token    unique string representing the token
     * @param clientId the client id for which the token was issued
     * @return the token information
     */
    @POST
    @Path("info")
    public Response tokenInfo(@FormParam("token") String token, @FormParam("clientId") String clientId) {
        if (token == null || clientId == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "'token' and 'clientId' form parameters are required.");
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

    @Override
    protected boolean usesSessions() {
        return false;
    }
}
