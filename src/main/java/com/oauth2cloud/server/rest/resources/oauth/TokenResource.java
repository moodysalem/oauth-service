package com.oauth2cloud.server.rest.resources.oauth;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.hibernate.model.*;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.models.ErrorResponse;
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

@NoXFrameOptionsFeature.NoXFrame
@Path(OAuth2Application.OAUTH + "/token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TokenResource extends OAuthResource {

    private static final String BASIC = "Basic ";
    private static final int BASIC_LENGTH = BASIC.length();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String PASSWORD = "password";
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String TEMPORARY_TOKEN = "temporary_token";

    @HeaderParam("Authorization")
    private String authorizationHeader;

    private Client client = null;

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (authorizationHeader != null) {
            if (authorizationHeader.startsWith(BASIC)) {
                String credentials = authorizationHeader.substring(BASIC_LENGTH);
                String decoded = new String(Base64.getDecoder().decode(credentials.getBytes(UTF8)), UTF8);
                String[] pieces = decoded.split(":");
                if (pieces.length == 2) {
                    String clientId = pieces[0].trim();
                    String secret = pieces[1].trim();
                    Client tempClient = getClient(clientId);
                    if (tempClient != null && secret.equals(tempClient.getSecret())) {
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
            case REFRESH_TOKEN:
                return refreshTokenGrantType(formParams);
            case TEMPORARY_TOKEN:
                return temporaryTokenGrantType(formParams);
            default:
                return error(ErrorResponse.Type.unsupported_grant_type, "The 'grant_type' specified is not supported by this server.");
        }
    }

    private Response temporaryTokenGrantType(MultivaluedMap<String, String> formParams) {
        String token = formParams.getFirst("access_token");
        String clientId = formParams.getFirst("client_id");

        if (token == null || token.trim().isEmpty()) {
            return error(ErrorResponse.Type.invalid_request,
                    String.format("'%s' grant type requires the 'token' parameter", TEMPORARY_TOKEN));
        }

        if (clientId == null || clientId.trim().isEmpty()) {
            return error(ErrorResponse.Type.invalid_request,
                    String.format("'%s' grant type requires the 'client_id' parameter", TEMPORARY_TOKEN));
        }

        Client c = getClient(clientId);
        if (c == null) {
            return error(ErrorResponse.Type.invalid_client, "Invalid 'client_id.'");
        }
        logCall(c);

        if (!c.getFlows().contains(Client.GrantFlow.TEMPORARY_TOKEN)) {
            return error(ErrorResponse.Type.unauthorized_client,
                    String.format("Client is not authorized for the '%s' grant flow.", TEMPORARY_TOKEN));
        }

        Token accessToken = getToken(token, c, Token.Type.ACCESS);
        if (accessToken == null) {
            return error(ErrorResponse.Type.invalid_grant, "Invalid or expired access token.");
        }

        List<AcceptedScope> newAcceptedScopes = new ArrayList<>(accessToken.getAcceptedScopes());
        List<ClientScope> newClientScopes = new ArrayList<>(accessToken.getClientScopes());

        Token tempToken = generateToken(Token.Type.TEMPORARY, c, accessToken.getUser(), getExpires(c, Token.Type.TEMPORARY),
                accessToken.getRedirectUri(), newAcceptedScopes, null, newClientScopes, accessToken.getProvider(), accessToken.getProviderAccessToken());

        return noCache(Response.ok(TokenResponse.from(tempToken))).build();
    }

    private Response refreshTokenGrantType(MultivaluedMap<String, String> formParams) {
        String token = formParams.getFirst("refresh_token");
        String scope = formParams.getFirst("scope");

        if (client == null) {
            return error(ErrorResponse.Type.invalid_grant, "Client authorization is required for the '" + REFRESH_TOKEN + "' grant type.");
        }

        if (!client.getType().equals(Client.Type.CONFIDENTIAL)) {
            return error(ErrorResponse.Type.invalid_grant, "Client type must be confidential for the '" + REFRESH_TOKEN + "' grant type.");
        }

        if (token == null) {
            return error(ErrorResponse.Type.invalid_request, "'refresh_token' parameter is required.");
        }

        Token refreshToken = getToken(token, client, Token.Type.REFRESH);
        if (refreshToken == null) {
            return error(ErrorResponse.Type.invalid_grant, "Invalid or expired refresh token.");
        }
        logCall(refreshToken.getClient());

        List<AcceptedScope> newTokenScopes = new ArrayList<>(refreshToken.getAcceptedScopes());

        List<String> scopes = scopeList(scope);
        if (scopes.size() > 0) {
            Set<String> acceptedScopeNames = newTokenScopes.stream().map(AcceptedScope::getClientScope)
                    .map(ClientScope::getScope).map(Scope::getName).collect(Collectors.toSet());

            if (!acceptedScopeNames.containsAll(scopes)) {
                List<ClientScope> clientScopes = newTokenScopes.stream().map(AcceptedScope::getClientScope).collect(Collectors.toList());
                String invalidScopes = getMissingScopes(clientScopes, scopes);
                return error(ErrorResponse.Type.invalid_scope, "The following scopes were invalid for the refresh token: " + invalidScopes);
            }
        }

        Token accessToken = generateToken(Token.Type.ACCESS, client, refreshToken.getUser(), getExpires(client, Token.Type.ACCESS),
                refreshToken.getRedirectUri(), newTokenScopes, refreshToken, null, refreshToken.getProvider(), refreshToken.getProviderAccessToken());

        return noCache(Response.ok(TokenResponse.from(accessToken))).build();
    }

    private Response clientCredentiaslGrantType(MultivaluedMap<String, String> formParams) {
        String scope = formParams.getFirst("scope");

        if (client == null) {
            return error(ErrorResponse.Type.invalid_client, "Client authorization failed.");
        }
        logCall(client);

        if (!client.getFlows().contains(Client.GrantFlow.CLIENT_CREDENTIALS)) {
            return error(ErrorResponse.Type.unauthorized_client,
                    String.format("Client is not authorized for the '%s' grant flow.", CLIENT_CREDENTIALS));
        }

        if (!client.getType().equals(Client.Type.CONFIDENTIAL)) {
            return error(ErrorResponse.Type.unauthorized_client,
                    String.format("Client must be %s for the '%s' grant flow.", Client.Type.CONFIDENTIAL.toString(), CLIENT_CREDENTIALS));
        }

        List<String> scopes = scopeList(scope);

        List<ClientScope> clientScopes = getScopes(client, scopes);
        if (scopes.size() > 0 && clientScopes.size() != scopes.size()) {
            String invalidScopes = getMissingScopes(clientScopes, scopes);
            return error(ErrorResponse.Type.invalid_scope, "The following scopes were invalid: " + invalidScopes);
        }

        Token token = generateToken(Token.Type.CLIENT, client, null, getExpires(client, Token.Type.CLIENT), null, null, null, clientScopes, null, null);

        return noCache(Response.ok(TokenResponse.from(token))).build();
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
                    "'code', 'redirect_uri', and 'client_id' are all required for the " + AUTHORIZATION_CODE + " grant flow.");
        }

        Client client = getClient(clientId);
        if (client == null) {
            return error(ErrorResponse.Type.invalid_client, "Invalid client ID.");
        }
        logCall(client);

        if (!client.getFlows().contains(Client.GrantFlow.CODE)) {
            return error(ErrorResponse.Type.unauthorized_client, "Client is not authorized for the '" + AUTHORIZATION_CODE + "' grant flow.");
        }

        if (this.client != null && !this.client.equals(client)) {
            return error(ErrorResponse.Type.invalid_client, "Client authentication does not match client ID.");
        }

        if (client.getType().equals(Client.Type.CONFIDENTIAL) && this.client == null) {
            return error(ErrorResponse.Type.invalid_client,
                    String.format("Client authentication is required for CONFIDENTIAL clients in the '%s' grant flow.",
                            AUTHORIZATION_CODE));
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
            refreshToken = generateToken(Token.Type.REFRESH, client, codeToken.getUser(), getExpires(client, Token.Type.REFRESH), redirectUri,
                    new ArrayList<>(codeToken.getAcceptedScopes()), null, null, codeToken.getProvider(), codeToken.getProviderAccessToken());
        }
        Token accessToken = generateToken(Token.Type.ACCESS, client, codeToken.getUser(), getExpires(client, Token.Type.ACCESS), redirectUri,
                new ArrayList<>(codeToken.getAcceptedScopes()), refreshToken, null, codeToken.getProvider(), codeToken.getProviderAccessToken());

        return noCache(Response.ok(TokenResponse.from(accessToken))).build();
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
        logCall(client);

        if (!client.getType().equals(Client.Type.CONFIDENTIAL)) {
            return error(ErrorResponse.Type.invalid_client, "Client must be CONFIDENTIAL to use this the '" + PASSWORD + "' grant type.");
        }

        if (!client.getFlows().contains(Client.GrantFlow.RESOURCE_OWNER_CREDENTIALS)) {
            return error(ErrorResponse.Type.unauthorized_client, "Client is not authorized for the '" + PASSWORD + "' grant type.");
        }

        boolean valid = false;
        User user = getUser(username, client);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            valid = true;
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
            refreshToken = generateToken(Token.Type.REFRESH, client, user, getExpires(client, Token.Type.REFRESH), null,
                    new ArrayList<>(acceptedScopes), null, null, null, null);
        }
        Token accessToken = generateToken(Token.Type.ACCESS, client, user, getExpires(client, Token.Type.ACCESS), null,
                new ArrayList<>(acceptedScopes), refreshToken, null, null, null);

        return noCache(Response.ok(TokenResponse.from(accessToken))).build();
    }

    private String getMissingScopes(List<ClientScope> clientScopes, List<String> requestedScopes) {
        // list of client scope names returned
        Set<String> clientScopeNames = clientScopes.stream().map(ClientScope::getScope).map(Scope::getName).collect(Collectors.toSet());
        // list of scopes requested minus the client scope names returned
        return requestedScopes.stream().filter((s) -> !clientScopeNames.contains(s)).collect(Collectors.joining("; "));
    }

    private User getUser(String username, Client client) {
        CriteriaQuery<User> uq = cb.createQuery(User.class);
        Root<User> userRoot = uq.from(User.class);
        uq.select(userRoot).where(
                cb.equal(userRoot.get(User_.email), username),
                cb.equal(userRoot.join(User_.application), client.getApplication())
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

    /**
     * Get the info for a token for a specific client
     *
     * @param token         unique string representing the token
     * @param applicationId the id of the application for which this token was created
     * @return the token information
     */
    @POST
    @Path("info")
    public Response tokenInfo(@FormParam("token") String token, @FormParam("client_id") String clientId,
                              @FormParam("application_id") UUID applicationId) {
        if (token == null || (applicationId == null && clientId == null)) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                    "'token' and the 'application_id' or 'client_id' form parameters are required.");
        }

        Client c = null;
        if (clientId != null) {
            c = getClient(clientId);
            if (c == null) {
                throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid client ID.");
            }
            logCall(c);
            applicationId = c.getApplication().getId();
        }

        Token t = getToken(token, c, Token.Type.ACCESS, Token.Type.REFRESH, Token.Type.TEMPORARY);
        if (t == null || !t.getClient().getApplication().getId().equals(applicationId)) {
            throw new RequestProcessingException(Response.Status.NOT_FOUND, "Token not found or expired.");
        }

        // call made on behalf of a client
        if (clientId != null) {
            logCall(t.getClient());
        } else {
            // otherwise call made on behalf of the application
            logCall(t.getClient().getApplication());
        }

        return noCache(Response.ok(TokenResponse.from(t))).build();
    }

    private Response.ResponseBuilder noCache(Response.ResponseBuilder rb) {
        return rb.header("Cache-Control", "no-store").header("Pragma", "no-cache");
    }

}
