package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.rest.util.QueryUtil;
import com.oauth2cloud.server.model.api.ErrorResponse;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;

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

import static com.oauth2cloud.server.model.db.Token.getExpires;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@NoXFrameOptionsFeature.NoXFrame
@Path(OAuth2Application.OAUTH_PATH + "/token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TokenResource extends OAuthResource {

    private static final String BASIC = "Basic ";
    private static final int BASIC_LENGTH = BASIC.length();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String PASSWORD = "password",
            CLIENT_CREDENTIALS = "client_credentials",
            REFRESH_TOKEN = "refresh_token",
            TEMPORARY_TOKEN = "temporary_token";

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
                    Client tempClient = QueryUtil.getClient(em, clientId);
                    if (tempClient != null && secret.equals(tempClient.getCredentials().getSecret())) {
                        client = tempClient;
                    }
                }
            }
        }
    }

    private Response error(final int statusCode, final ErrorResponse.Type type, final String description, final String uri) {
        return Response.status(statusCode)
                .entity(new ErrorResponse(type, description, uri))
                .build();
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
        final String token = formParams.getFirst("access_token"),
                clientId = formParams.getFirst("client_id");

        if (isEmpty(token)) {
            return error(ErrorResponse.Type.invalid_request,
                    String.format("'%s' grant type requires the 'token' parameter", TEMPORARY_TOKEN));
        }

        if (isEmpty(clientId)) {
            return error(ErrorResponse.Type.invalid_request,
                    String.format("'%s' grant type requires the 'client_id' parameter", TEMPORARY_TOKEN));
        }

        final Client c = QueryUtil.getClient(em, clientId);
        if (c == null) {
            return error(ErrorResponse.Type.invalid_client, "Invalid 'client_id.'");
        }
        QueryUtil.logCall(em, c, req);

        if (!c.getFlows().contains(GrantFlow.TEMPORARY_TOKEN)) {
            return error(ErrorResponse.Type.unauthorized_client,
                    String.format("Client is not authorized for the '%s' grant flow.", TEMPORARY_TOKEN));
        }

        final Token accessToken = QueryUtil.findToken(em, token, c, Collections.singleton(TokenType.ACCESS));
        if (accessToken == null) {
            return error(ErrorResponse.Type.invalid_grant, "Invalid or expired access token.");
        }

        final Set<AcceptedScope> newAcceptedScopes = new HashSet<>(accessToken.getAcceptedScopes());
        final Set<ClientScope> newClientScopes = new HashSet<>(accessToken.getClientScopes());

        Token tempToken = QueryUtil.generateToken(em, TokenType.TEMPORARY, c, accessToken.getUser(), getExpires(c, TokenType.TEMPORARY),
                accessToken.getRedirectUri(), newAcceptedScopes, null, newClientScopes);

        return noCache(Response.ok(TokenResponse.from(tempToken))).build();
    }

    private Response refreshTokenGrantType(MultivaluedMap<String, String> formParams) {
        final String token = formParams.getFirst("refresh_token"),
                scope = formParams.getFirst("scope");

        if (client == null) {
            return error(ErrorResponse.Type.invalid_grant, "Client authorization is required for the '" + REFRESH_TOKEN + "' grant type.");
        }

        if (!client.isConfidential()) {
            return error(ErrorResponse.Type.invalid_grant, "Client type must be confidential for the '" + REFRESH_TOKEN + "' grant type.");
        }

        if (token == null) {
            return error(ErrorResponse.Type.invalid_request, "'refresh_token' parameter is required.");
        }

        final Token refreshToken = QueryUtil.findToken(em, token, client, Collections.singleton(TokenType.REFRESH));
        if (refreshToken == null) {
            return error(ErrorResponse.Type.invalid_grant, "Invalid or expired refresh token.");
        }
        QueryUtil.logCall(em, refreshToken.getClient(), req);

        final Set<AcceptedScope> newTokenScopes = new HashSet<>(refreshToken.getAcceptedScopes());

        final Set<String> scopes = scopeList(scope);
        if (scopes.size() > 0) {
            final Set<String> acceptedScopeNames = newTokenScopes.stream().map(AcceptedScope::getClientScope)
                    .map(ClientScope::getScope).map(Scope::getName).collect(Collectors.toSet());

            if (!acceptedScopeNames.containsAll(scopes)) {
                final Set<ClientScope> clientScopes = newTokenScopes.stream().map(AcceptedScope::getClientScope).collect(Collectors.toSet());
                final String invalidScopes = getMissingScopes(clientScopes, scopes).stream().collect(Collectors.joining("; "));
                return error(ErrorResponse.Type.invalid_scope, "The following scopes were invalid for the refresh token: " + invalidScopes);
            }
        }

        Token accessToken = QueryUtil.generateToken(em, TokenType.ACCESS, client, refreshToken.getUser(), getExpires(client, TokenType.ACCESS),
                refreshToken.getRedirectUri(), newTokenScopes, refreshToken, null);

        return noCache(Response.ok(TokenResponse.from(accessToken))).build();
    }

    private Response clientCredentiaslGrantType(MultivaluedMap<String, String> formParams) {
        String scope = formParams.getFirst("scope");

        if (client == null) {
            return error(ErrorResponse.Type.invalid_client, "Client authorization failed.");
        }
        QueryUtil.logCall(em, client, req);

        if (!client.getFlows().contains(GrantFlow.CLIENT_CREDENTIALS)) {
            return error(ErrorResponse.Type.unauthorized_client,
                    String.format("Client is not authorized for the '%s' grant flow.", CLIENT_CREDENTIALS));
        }

        if (!client.isConfidential()) {
            return error(ErrorResponse.Type.unauthorized_client,
                    String.format("Client must be confidential for the '%s' grant flow.", CLIENT_CREDENTIALS));
        }

        final Set<String> scopes = scopeList(scope);

        final Set<ClientScope> clientScopes = QueryUtil.getScopes(em, client, scopes);

        final Set<String> missingScopes = getMissingScopes(clientScopes, scopes);

        if (!missingScopes.isEmpty()) {
            return error(ErrorResponse.Type.invalid_scope, "The following scopes were invalid: " + missingScopes.stream().collect(Collectors.joining("; ")));
        }

        final Token clientToken = QueryUtil.generateToken(em,
                TokenType.CLIENT, client, null,
                getExpires(client, TokenType.CLIENT),
                null, null, null, clientScopes
        );

        return noCache(Response.ok(TokenResponse.from(clientToken))).build();
    }

    /**
     * Split a string into the set of scope names
     *
     * @param scope
     * @return
     */
    private Set<String> scopeList(final String scope) {
        final Set<String> scopes = new HashSet<>();
        if (scope != null) {
            String[] splitScopes = scope.split(" ");
            for (final String s : splitScopes) {
                if (!isBlank(s)) {
                    scopes.add(s.trim());
                }
            }
        }
        return scopes;
    }

    private Response authorizationCodeGrantType(MultivaluedMap<String, String> formParams) {
        final String code = formParams.getFirst("code"),
                redirectUri = formParams.getFirst("redirect_uri"),
                clientId = formParams.getFirst("client_id");

        if (code == null || redirectUri == null || clientId == null) {
            return error(ErrorResponse.Type.invalid_request,
                    "'code', 'redirect_uri', and 'client_id' are all required for the " + AUTHORIZATION_CODE + " grant flow.");
        }

        final Client client = QueryUtil.getClient(em, clientId);
        if (client == null) {
            return error(ErrorResponse.Type.invalid_client, "Invalid client ID.");
        }
        QueryUtil.logCall(em, client, req);

        if (!client.getFlows().contains(GrantFlow.CODE)) {
            return error(ErrorResponse.Type.unauthorized_client, "Client is not authorized for the '" + AUTHORIZATION_CODE + "' grant flow.");
        }

        if (this.client != null && !this.client.idMatch(client)) {
            return error(ErrorResponse.Type.invalid_client, "Client authentication does not match client ID.");
        }

        if (client.isConfidential() && this.client == null) {
            return error(ErrorResponse.Type.invalid_client,
                    String.format("Client authentication is required for CONFIDENTIAL clients in the '%s' grant flow.",
                            AUTHORIZATION_CODE));
        }

        final Token codeToken = QueryUtil.findToken(em, code, client, Collections.singleton(TokenType.CODE));
        if (codeToken == null) {
            return error(ErrorResponse.Type.invalid_grant, "Invalid token.");
        }

        if (!redirectUri.equals(codeToken.getRedirectUri())) {
            return error(ErrorResponse.Type.invalid_grant, "Redirect URI must exactly match the original redirect UriUtil.");
        }

        // first expire the token
        expireToken(codeToken);

        Token refreshToken = null;
        // we know the token is valid, so we should generate an access token now
        // only confidential clients may receive refresh tokens
        if (client.getRefreshTokenTtl() != null && client.isConfidential()) {
            refreshToken = QueryUtil.generateToken(em, TokenType.REFRESH, client, codeToken.getUser(), getExpires(client, TokenType.REFRESH), redirectUri,
                    new HashSet<>(codeToken.getAcceptedScopes()), null, null);
        }
        final Token accessToken = QueryUtil.generateToken(em, TokenType.ACCESS, client, codeToken.getUser(), getExpires(client, TokenType.ACCESS), redirectUri,
                new HashSet<>(codeToken.getAcceptedScopes()), refreshToken, null);

        return noCache(Response.ok(TokenResponse.from(accessToken))).build();
    }

    private Response passwordGrantType(MultivaluedMap<String, String> formParams) {
        return error(ErrorResponse.Type.invalid_grant, "This OAuth2 server implementation does not support passwords");
    }

    private Set<String> getMissingScopes(final Set<ClientScope> clientScopes, final Set<String> requestedScopes) {
        // list of client scope names returned
        final Set<String> clientScopeNames = clientScopes.stream().map(ClientScope::getScope).map(Scope::getName)
                .collect(Collectors.toSet());

        // list of scopes requested minus the client scope names returned
        return requestedScopes.stream().filter((s) -> !clientScopeNames.contains(s)).collect(Collectors.toSet());
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
        try {
            TXHelper.withinTransaction(em, () -> {
                t.setExpires(new Date());
                em.merge(t);
            });
        } catch (Exception e) {
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
            c = QueryUtil.getClient(em, clientId);
            if (c == null) {
                throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid client ID.");
            }
            QueryUtil.logCall(em, c, req);
            applicationId = c.getApplication().getId();
        }

        final Token t = QueryUtil.findToken(em, token, c, Arrays.asList(TokenType.ACCESS, TokenType.REFRESH, TokenType.TEMPORARY));
        if (t == null || !t.getClient().getApplication().getId().equals(applicationId)) {
            throw new RequestProcessingException(Response.Status.NOT_FOUND, "Token not found or expired.");
        }

        // call made on behalf of a client
        if (clientId != null) {
            QueryUtil.logCall(em, t.getClient(), req);
        } else {
            // otherwise call made on behalf of the application
            QueryUtil.logCall(em, t.getClient().getApplication(), req);
        }

        return noCache(Response.ok(TokenResponse.from(t))).build();
    }

    private Response.ResponseBuilder noCache(Response.ResponseBuilder rb) {
        return rb.header("Cache-Control", "no-store").header("Pragma", "no-cache");
    }

}
