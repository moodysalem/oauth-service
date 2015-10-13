package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;
import com.leaguekit.oauth.model.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Path("token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TokenResource extends BaseResource {

    private static final String BASIC = "Basic ";
    private static final int BASIC_LENGTH = BASIC.length();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String PASSWORD = "password";
    private static final long THREE_SECONDS = 3000L;
    public static final String CLIENT_CREDENTIALS = "client_credentials";

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

    private Response error(int statusCode, ErrorResponse.Type type, String description, String uri) {
        ErrorResponse er = new ErrorResponse();
        er.setError(type);
        er.setErrorDescription(description);
        er.setErrorUri(uri);
        return Response.status(statusCode).entity(er).build();
    }

    private Response error(ErrorResponse.Type type, String description, String uri) {
        return error(400, type, description, uri);
    }

    private Response error(ErrorResponse.Type type, String description) {
        return error(400, type, description, null);
    }

    @POST
    public Response post(MultivaluedMap<String, String> formParams) {
        if (formParams == null) {
            return error(ErrorResponse.Type.invalid_request, "Invalid request body");
        }

        String grantType = formParams.getFirst("grant_type");
        if (grantType == null) {
            return error(ErrorResponse.Type.invalid_request, "'grant_type' is required.");
        }

        switch (grantType) {
            case AUTHORIZATION_CODE:
                return authorizationCodeGrantType(formParams);
            case PASSWORD:
                return passwordGrantType(formParams);
            case CLIENT_CREDENTIALS:
                return clientCredentiaslGrantType(formParams);
            default:
                return error(ErrorResponse.Type.invalid_grant, "Invalid 'grant_type' specified.");
        }
    }

    private Response clientCredentiaslGrantType(MultivaluedMap<String, String> formParams) {
        String scope = formParams.getFirst("scope");

        if (client == null) {
            return error(ErrorResponse.Type.invalid_client, "Client authorization failed.");
        }

        List<String> scopes = scopeList(scope);

        List<ClientScope> clientScopes = getScopes(client, scopes);
        if (scopes.size() > 0 && clientScopes.size() != scopes.size()) {
            String invalidScopes = getMissingScopes(clientScopes, scopes);
            return error(ErrorResponse.Type.invalid_scope, "The following scopes were invalid: " + invalidScopes);
        }

        Token token = generateToken(Token.Type.CLIENT, client, null, getExpires(client, false), null, null, null, clientScopes);

        return noCache(Response.ok(makeTokenResponse(token))).build();
    }

    private List<String> scopeList(String scope) {
        List<String> scopes = new ArrayList<>();
        if (scope != null) {
            String[] splitScopes = scope.split(" ");
            for (String s : splitScopes) {
                if (s != null && s.trim().length() > 0) {
                    scopes.add(s.trim());
                }
            }
        }
        return scopes;
    }

    private Response authorizationCodeGrantType(MultivaluedMap<String, String> formParams) {
        String code = formParams.getFirst("code");
        String redirectUri = formParams.getFirst("redirect_uri");
        String clientId = formParams.getFirst("client_id");

        if (code == null || redirectUri == null || clientId == null) {
            return error(ErrorResponse.Type.invalid_request,
                "'code', 'redirect_uri', and 'client_id' are all required for the " + AUTHORIZATION_CODE + " grant type.");
        }

        Client client = getClient(clientId);
        if (client == null) {
            return error(ErrorResponse.Type.invalid_client, "Invalid client ID.");
        }

        if (!client.getFlows().contains(Client.GrantFlow.CODE)) {
            return error(ErrorResponse.Type.unauthorized_client, "Client is not authorized for the '" + AUTHORIZATION_CODE + "' grant type.");
        }

        if (this.client != null && !this.client.equals(client)) {
            return error(ErrorResponse.Type.invalid_client, "Client authentication does not match client ID.");
        }

        if (client.getType().equals(Client.Type.CONFIDENTIAL) && this.client == null) {
            return error(ErrorResponse.Type.invalid_client, "Client authentication is required for confidential clients.");
        }

        Token codeToken = getToken(code, client, Token.Type.CODE);
        if (codeToken == null) {
            return error(ErrorResponse.Type.invalid_grant, "Invalid token.");
        }

        if (!redirectUri.equals(codeToken.getRedirectUri())) {
            return error(ErrorResponse.Type.invalid_grant, "Redirect URI must exactly match the original redirect URI.");
        }

        // first expire the token
        expireToken(codeToken);

        Token refreshToken = null;
        // we know the token is valid, so we should generate an access token now
        // only confidential clients may receive refresh tokens
        if (client.getRefreshTokenTtl() != null && client.getType().equals(Client.Type.CONFIDENTIAL)) {
            refreshToken = generateToken(Token.Type.REFRESH, client, codeToken.getUser(), getExpires(client, true), redirectUri,
                new ArrayList<>(codeToken.getAcceptedScopes()), null, null);
        }
        Token accessToken = generateToken(Token.Type.ACCESS, client, codeToken.getUser(), getExpires(client, false), redirectUri,
            new ArrayList<>(codeToken.getAcceptedScopes()), refreshToken, null);

        return noCache(Response.ok(makeTokenResponse(accessToken))).build();
    }

    private Response passwordGrantType(MultivaluedMap<String, String> formParams) {
        String username = formParams.getFirst("username");
        String password = formParams.getFirst("password");
        String scope = formParams.getFirst("scope");

        if (username == null || password == null) {
            return error(ErrorResponse.Type.invalid_request, "'username' and 'password' are required parameters.");
        }

        if (client == null) {
            return error(ErrorResponse.Type.invalid_client, "Client authentication is ALWAYS required for the '" + PASSWORD + "' grant type.");
        }

        if (!client.getType().equals(Client.Type.CONFIDENTIAL)) {
            return error(ErrorResponse.Type.invalid_client, "Client must be CONFIDENTIAL to use this the '" + PASSWORD + "' grant type.");
        }

        if (!client.getFlows().contains(Client.GrantFlow.RESOURCE_OWNER_CREDENTIALS)) {
            return error(ErrorResponse.Type.unauthorized_client, "Client is not authorized for the '" + PASSWORD + "' grant type.");
        }

        boolean valid = false;
        // this part of the code ALWAYS takes one second because it checks the user credentials, preventing against brute
        // forcing and timing attacks
        long t1 = System.currentTimeMillis();
        User user = getUser(username, client);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            valid = true;
        }
        long t2 = System.currentTimeMillis();
        try {
            long sleepTime = THREE_SECONDS - (t2 - t1);
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Thread interrupted during wait.", e);
        }

        if (!valid) {
            return error(ErrorResponse.Type.invalid_grant, "Invalid username or password.");
        }

        List<String> scopes = scopeList(scope);

        List<ClientScope> clientScopes = getScopes(client, scopes);

        if (scopes.size() > 0 && clientScopes.size() < scopes.size()) {
            String invalidScopes = getMissingScopes(clientScopes, scopes);
            return error(ErrorResponse.Type.invalid_scope, "The following scopes were invalid: " + invalidScopes);
        }


        List<AcceptedScope> acceptedScopes = new ArrayList<>();
        for (ClientScope cs : clientScopes) {
            acceptedScopes.add(acceptScope(user, cs));
        }

        Token refreshToken = null;
        if (client.getRefreshTokenTtl() != null) {
            refreshToken = generateToken(Token.Type.REFRESH, client, user, getExpires(client, true), null,
                new ArrayList<>(acceptedScopes), null, null);
        }
        Token accessToken = generateToken(Token.Type.ACCESS, client, user, getExpires(client, false), null,
            new ArrayList<>(acceptedScopes), refreshToken, null);

        return noCache(Response.ok(makeTokenResponse(accessToken))).build();
    }

    private String getMissingScopes(List<ClientScope> clientScopes, List<String> requestedScopes) {
        // list of client scope names returned
        Set<String> clientScopeNames = clientScopes.stream().map(ClientScope::getScope).map(Scope::getName).collect(Collectors.toSet());
        // list of scopes requested minus the client scope names returned
        return requestedScopes.stream().filter((s) -> !clientScopeNames.contains(s)).collect(Collectors.joining("; "));
    }

    private User getUser(String username, Client client) {
        CriteriaQuery<User> uq = cb.createQuery(User.class);
        Root<User> ur = uq.from(User.class);
        uq.select(ur).where(
            cb.equal(ur.get("email"), username),
            cb.equal(ur.join("application"), client.getApplication())
        );
        List<User> users = em.createQuery(uq).getResultList();
        return users.size() == 1 ? users.get(0) : null;
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
            LOG.log(Level.SEVERE, "Failed to expire token", e);
        }
    }

    private TokenResponse makeTokenResponse(Token accessToken) {
        TokenResponse tr = new TokenResponse();
        tr.setAccessToken(accessToken.getToken());
        tr.setExpiresIn(accessToken.getExpiresIn());
        if (accessToken.getRefreshToken() != null) {
            tr.setRefreshToken(accessToken.getRefreshToken().getToken());
        }
        tr.setScope(accessToken.getScope());
        tr.setTokenType(BEARER);
        return tr;
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

        return noCache(Response.ok(makeTokenResponse(t))).build();
    }

    private Response.ResponseBuilder noCache(Response.ResponseBuilder rb) {
        return rb.header("Cache-Control", "no-store").header("Pragma", "no-cache");
    }

    @Override
    protected boolean usesSessions() {
        return false;
    }
}
