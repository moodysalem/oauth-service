package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.api.ErrorResponse;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.util.CallLogUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.oauth2cloud.server.model.api.ErrorResponse.Type.*;
import static com.oauth2cloud.server.rest.util.OAuthUtil.parseScope;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@NoXFrameOptionsFeature.NoXFrame
@Path("token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TokenResource extends BaseResource {
    public enum GrantType {
        authorization_code,
        password,
        client_credentials,
        refresh_token
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private Response error(final int statusCode, final ErrorResponse.Type type, final String description, final String uri) {
        return Response.status(statusCode)
                .entity(new ErrorResponse(type, description, uri))
                .build();
    }

    private Response error(ErrorResponse.Type type, String description) {
        return error(400, type, description, null);
    }

    private static final String BASIC = "Basic ";
    private static final int BASIC_LENGTH = BASIC.length();

    private Client readAuthorizationHeader(final String authorizationHeader) {
        if (authorizationHeader != null) {
            if (authorizationHeader.startsWith(BASIC)) {
                final String credentials = authorizationHeader.substring(BASIC_LENGTH);
                final String decoded = new String(Base64.getDecoder().decode(credentials.getBytes(UTF8)), UTF8);
                final String[] pieces = decoded.split(":");
                if (pieces.length == 2) {
                    final String clientId = pieces[0].trim();
                    final String secret = pieces[1].trim();
                    final Client withId = QueryUtil.getClient(em, clientId);
                    if (withId != null && secret.equals(withId.getCredentials().getSecret())) {
                        return withId;
                    }
                }
            }
        }

        return null;
    }

    @POST
    public Response post(
            @HeaderParam("Authorization") final String authorizationHeader,
            @FormParam("grant_type") final String grantTypeString,
            @FormParam("refresh_token") final String refreshToken,
            @FormParam("scope") final String scope,
            @FormParam("code") final String code,
            @FormParam("redirect_uri") final String redirectUri,
            @FormParam("client_id") final String clientId,
            @FormParam("email") final String email
    ) {
        if (isEmpty(grantTypeString)) {
            return error(invalid_request, "'grant_type' is required.");
        }

        final GrantType grantType;
        try {
            grantType = GrantType.valueOf(grantTypeString);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Invalid grant type passed", e);
            return error(ErrorResponse.Type.unsupported_grant_type,
                    "The 'grant_type' specified is not supported by this server.");
        }

        switch (grantType) {
            case authorization_code:
                return authorizationCodeGrantType(authorizationHeader, code, redirectUri, clientId);
            case password:
                return passwordGrantType(email);
            case client_credentials:
                return clientCredentialsGrantType(authorizationHeader, scope);
            case refresh_token:
                return refreshTokenGrantType(authorizationHeader, refreshToken, scope);
            default:
                return error(ErrorResponse.Type.unsupported_grant_type, "Unrecognized 'grant_type'");
        }
    }

    private Response refreshTokenGrantType(final String authorizationHeader, final String token, final String scope) {
        final Client client = readAuthorizationHeader(authorizationHeader);

        if (client == null) {
            return error(invalid_grant, "Client authorization is required for the 'refresh_token' grant type.");
        }

        if (!client.isConfidential()) {
            return error(invalid_grant, "Client type must be confidential for the 'refresh_token' grant type.");
        }

        if (token == null) {
            return error(invalid_request, "'refresh_token' parameter is required.");
        }

        final UserRefreshToken refreshToken = QueryUtil.findToken(em, token, client, UserRefreshToken.class);
        if (refreshToken == null) {
            return error(invalid_grant, "Invalid or expired refresh token.");
        }
        CallLogUtil.logCall(em, refreshToken.getClient(), req);

        final Set<AcceptedScope> newTokenScopes = new HashSet<>(refreshToken.getAcceptedScopes());

        final Set<String> scopes = parseScope(scope);
        if (scopes.size() > 0) {
            final Set<String> acceptedScopeNames = newTokenScopes.stream().map(AcceptedScope::getClientScope)
                    .map(ClientScope::getScope).map(Scope::getName).collect(Collectors.toSet());

            if (!acceptedScopeNames.containsAll(scopes)) {
                final Set<ClientScope> clientScopes = newTokenScopes.stream().map(AcceptedScope::getClientScope).collect(Collectors.toSet());
                final String invalidScopes = getMissingScopes(clientScopes, scopes).stream().collect(Collectors.joining("; "));
                return error(invalid_scope, "The following scopes were invalid for the refresh token: " + invalidScopes);
            }
        }

        final UserAccessToken userAccessToken = new UserAccessToken();
        userAccessToken.setClient(client);
        userAccessToken.setUser(refreshToken.getUser());
        userAccessToken.setRedirectUri(refreshToken.getRedirectUri());
        userAccessToken.setAcceptedScopes(newTokenScopes);
        userAccessToken.setRefreshToken(refreshToken);

        final UserAccessToken saved;
        try {
            saved = TXHelper.withinTransaction(em, () -> em.merge(userAccessToken));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to save token in refresh grant type", e);
            return internalServerError();
        }

        return noCache(Response.ok(TokenResponse.from(saved))).build();
    }

    private Response clientCredentialsGrantType(final String authorizationHeader, final String scope) {
        final Client client = readAuthorizationHeader(authorizationHeader);
        if (client == null) {
            return error(invalid_client, "Client authorization failed.");
        }

        CallLogUtil.logCall(em, client, req);

        if (!client.getFlows().contains(GrantFlow.CLIENT_CREDENTIALS)) {
            return error(unauthorized_client,
                    "Client is not authorized for the 'client_credentials' grant flow.");
        }

        if (!client.isConfidential()) {
            return error(unauthorized_client,
                    "Client must be confidential to use the 'client_credentials' grant flow.");
        }

        final Set<String> scopes = parseScope(scope);

        final Set<ClientScope> clientScopes = QueryUtil.getScopes(em, client, scopes);

        final Set<String> missingScopes = getMissingScopes(clientScopes, scopes);

        if (!missingScopes.isEmpty()) {
            return error(ErrorResponse.Type.invalid_scope, "The following scopes were invalid: " + missingScopes.stream().collect(Collectors.joining("; ")));
        }

        final ClientToken saved;
        {
            final ClientToken clientToken = new ClientToken();
            clientToken.setClient(client);
            clientToken.setClientScopes(clientScopes);
            try {
                saved = TXHelper.withinTransaction(em, () -> em.merge(clientToken));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to save client token", e);
                return error(invalid_request, "Internal server error occurred!");
            }
        }

        return noCache(Response.ok(TokenResponse.from(saved))).build();
    }

    private Response authorizationCodeGrantType(final String authorizationHeader, final String code, final String redirectUri, final String clientId) {
        if (code == null || redirectUri == null || clientId == null) {
            return error(invalid_request,
                    "'code', 'redirect_uri', and 'client_id' are all required for the 'authorization_code' grant flow.");
        }

        final Client client = QueryUtil.getClient(em, clientId);
        if (client == null) {
            return error(invalid_client, "Invalid client ID.");
        }
        CallLogUtil.logCall(em, client, req);

        if (!client.getFlows().contains(GrantFlow.CODE)) {
            return error(unauthorized_client, "Client is not authorized for the 'authorization_code' grant flow.");
        }

        final Client headerClient = readAuthorizationHeader(authorizationHeader);

        if (headerClient != null && !headerClient.idMatch(client)) {
            return error(invalid_client, "Client authentication does not match client ID.");
        }

        if (client.isConfidential() && headerClient == null) {
            return error(invalid_client,
                    "Client authentication is required for CONFIDENTIAL clients in the 'authorization_code' grant flow.");
        }


        final UserAccessCode codeToken = QueryUtil.findToken(em, code, client, UserAccessCode.class);

        if (codeToken == null) {
            return error(invalid_grant, "Invalid access code.");
        }

        if (!redirectUri.equals(codeToken.getRedirectUri())) {
            return error(invalid_grant, "Redirect URI must exactly match the original redirect UriUtil.");
        }

        if (codeToken.isUsed()) {
            return error(invalid_grant, "Access code has already been used.");
        }

        // first expire the token
        try {
            TXHelper.withinTransaction(em, () -> {
                codeToken.setUsed(true);
                em.merge(codeToken);
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to use an access code", e);
            return internalServerError();
        }

        final UserRefreshToken refreshToken;
        // we know the token is valid, so we should generate an access token now
        // only confidential clients may receive refresh tokens
        if (client.getRefreshTokenTtl() != null && client.isConfidential()) {
            final UserRefreshToken token = new UserRefreshToken();
            token.setClient(client);
            token.setUser(codeToken.getUser());
            token.setRedirectUri(redirectUri);
            token.setAcceptedScopes(new HashSet<>(codeToken.getAcceptedScopes()));
            try {
                refreshToken = TXHelper.withinTransaction(em, () -> em.merge(token));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to create refresh token", e);
                return internalServerError();
            }
        } else {
            refreshToken = null;
        }

        final UserAccessToken accessToken;
        {
            final UserAccessToken token = new UserAccessToken();
            token.setClient(client);
            token.setUser(codeToken.getUser());
            token.setRedirectUri(redirectUri);
            token.setRefreshToken(refreshToken);
            token.setAcceptedScopes(new HashSet<>(codeToken.getAcceptedScopes()));
            try {
                accessToken = TXHelper.withinTransaction(em, () -> em.merge(token));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to create access token", e);
                return internalServerError();
            }
        }

        return noCache(Response.ok(TokenResponse.from(accessToken))).build();
    }

    private Response internalServerError() {
        return error(invalid_grant, "Internal server error!");
    }

    private Response passwordGrantType(final String email) {
        // TODO: send an e-mail to the user with a redirect url without forcing the user to go to
        return error(invalid_grant, "This OAuth2 server implementation does not support passwords");
    }

    private Set<String> getMissingScopes(final Set<ClientScope> clientScopes, final Set<String> requestedScopes) {
        // list of client scope names returned
        final Set<String> clientScopeNames = clientScopes.stream().map(ClientScope::getScope).map(Scope::getName)
                .collect(Collectors.toSet());

        // list of scopes requested minus the client scope names returned
        return requestedScopes.stream().filter((s) -> !clientScopeNames.contains(s)).collect(Collectors.toSet());
    }

    /**
     * Get the info for a token for a specific client
     *
     * @param tokenString   unique string representing the token
     * @param applicationId the id of the application for which this token was created
     * @return the token information
     */
    @POST
    @Path("info")
    public Response tokenInfo(
            @FormParam("token") final String tokenString,
            @FormParam("client_id") final String clientId,
            @FormParam("application_id") final UUID applicationId
    ) {
        if (isBlank(tokenString) || (applicationId == null && clientId == null)) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST,
                    "'token' and the 'application_id' or 'client_id' form parameters are required.");
        }

        final Client client;
        final Application application;

        if (clientId != null) {
            client = QueryUtil.getClient(em, clientId);
            if (client == null) {
                throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid client ID.");
            }
            application = null;
            CallLogUtil.logCall(em, client, req);
        } else {
            application = em.find(Application.class, applicationId);
            if (application == null) {
                throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid application ID");
            }
            client = null;
            CallLogUtil.logCall(em, application, req);
        }

        final Token token = QueryUtil.findToken(em, tokenString, client);

        if (token == null ||
                (client != null && !client.idMatch(token.getClient())) ||
                (application != null && !application.idMatch(token.getClient().getApplication()))) {
            throw new RequestProcessingException(Response.Status.NOT_FOUND, "Token not found or expired.");
        }

        return noCache(Response.ok(TokenResponse.from(token))).build();
    }

    private static Response.ResponseBuilder noCache(Response.ResponseBuilder rb) {
        return rb.header("Cache-Control", "no-store").header("Pragma", "no-cache");
    }

}
