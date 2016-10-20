package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.api.ErrorResponse;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.util.CallLogUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;
import io.swagger.annotations.*;

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

@Api("oauth2")
@NoXFrameOptionsFeature.NoXFrame
@Path("token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TokenResource extends BaseResource {
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

    private Client parseAuthorizationHeader(final String authorizationHeader) {
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

    @ApiOperation(
            value = "Authorization Code Grant Flow",
            notes = "Exchange an access code retrieved from the authorize resource for an access token"
    )
    @ApiResponses({
            @ApiResponse(code = 200, response = TokenResponse.class, message = "Success if the code is valid and the request is well formed"),
            @ApiResponse(code = 400, response = ErrorResponse.class, message = "Error if the request is not valid")
    })
    @POST
    @Path("authorization_code")
    public Response authorizationCode(
            @ApiParam(value = "Base64 encoded client_id:secret. Required for confidential clients only", required = true)
            @HeaderParam("Authorization") final String authorizationHeader,
            @ApiParam(value = "The code received from the authorization code grant flow to be exchanged for an access token", required = true)
            @FormParam("code") final String code,
            @ApiParam(value = "The redirect URI used to retrieve an authorization code", required = true)
            @FormParam("redirect_uri") final String redirectUri,
            @ApiParam(value = "The identifier of the client retrieving the authorization code", required = true)
            @FormParam("client_id") final String clientId
    ) {
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

        final Client headerClient = parseAuthorizationHeader(authorizationHeader);

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


    @ApiOperation(
            value = "Resource Owner Password Grant Flow",
            notes = "Retrieve an access token via the resource owner's credentials. This flow is not currently supported for this OAuth2 server implementation because users do not authenticate with passwords"
    )
    @ApiResponses({
            @ApiResponse(code = 400, response = ErrorResponse.class, message = "Always returns invalid_grant error")
    })
    @POST
    @Path("password")
    public Response password() {
        // TODO: send an e-mail to the user with a redirect url without forcing the user to go to
        return error(invalid_grant, "This OAuth2 server implementation does not yet support the password flow");
    }


    @ApiOperation(
            value = "Refresh Token Grant Flow",
            notes = "Use a refresh token to create a new access token"
    )
    @ApiResponses({
            @ApiResponse(code = 200, response = TokenResponse.class, message = "Returns the new access token if the request is well formed and the refresh token has not expired"),
            @ApiResponse(code = 400, response = ErrorResponse.class, message = "If the request is not well formed, or scopes are requested that are not associated with the refresh token, or the token has expired")
    })
    @POST
    @Path("refresh_token")
    public Response refreshTokenGrantType(
            @ApiParam(value = "Basic authorization scheme is required in some cases to identify that the request is coming from a specific client", required = true)
            @HeaderParam("Authorization") final String authorizationHeader,
            @ApiParam(value = "Refresh token required for refresh_token grant flow", required = true)
            @FormParam("refresh_token") final String refreshTokenString,
            @ApiParam(value = "Scope of the token being requested")
            @FormParam("scope") final String scope
    ) {
        final Client client = parseAuthorizationHeader(authorizationHeader);

        if (client == null) {
            return error(invalid_grant, "Client authorization is required for the 'refresh_token' grant type.");
        }

        if (!client.isConfidential()) {
            return error(invalid_grant, "Client type must be confidential for the 'refresh_token' grant type.");
        }

        if (refreshTokenString == null) {
            return error(invalid_request, "'refresh_token' parameter is required.");
        }

        final UserRefreshToken refreshToken = QueryUtil.findToken(em, refreshTokenString, client, UserRefreshToken.class);
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

    @ApiOperation(
            value = "Client Credentials Grant Flow",
            notes = "This endpoint provides client tokens in exchange for client credentials"
    )
    @ApiResponses({
            @ApiResponse(code = 200, response = TokenResponse.class, message = "If the request is well formed and the client supports the grant flow"),
            @ApiResponse(code = 400, response = ErrorResponse.class, message = "If the client does not have the client credentials grant flow or lacks the requested scopes")
    })
    @POST
    @Path("client_credentials")
    public Response clientCredentialsGrantType(
            @ApiParam(value = "Base64 encoded client_id:secret used to authenticate the client", required = true)
            @HeaderParam("Authorization") final String authorizationHeader,
            @ApiParam(value = "Scope of the token being requested")
            @FormParam("scope") final String scope
    ) {
        final Client client = parseAuthorizationHeader(authorizationHeader);
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

    private Response internalServerError() {
        return error(invalid_grant, "Internal server error!");
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
    @ApiOperation(
            value = "Token Info",
            notes = "This endpoint is used to retrieve information about a token. The server should use it to find the scopes associated with a token. Either client_id or application_id are required"
    )
    @POST
    @Path("info")
    public Response tokenInfo(
            @ApiParam(value = "The token to be found", required = true)
            @FormParam("token") final String tokenString,
            @ApiParam(value = "The identifier of the client", required = true)
            @FormParam("client_id") final String clientId,
            @ApiParam(value = "The ID of the application", required = true)
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
