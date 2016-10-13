package com.oauth2cloud.server.rest.util;

import com.oauth2cloud.server.model.data.ClientErrorModel;
import com.oauth2cloud.server.model.db.*;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class OAuthUtil {
    /**
     * Given a scope string which is a space delimited list of scopes, return a set of scope names
     *
     * @param scope space delimited scope string
     * @return set of scopes
     */
    public static Set<String> parseScope(final String scope) {
        if (isBlank(scope)) {
            return Collections.emptySet();
        } else {
            return Stream.of(scope.split(" "))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * This method validates all the query parameters and returns an error if anything is wrong with the authorization
     * request
     *
     * @param responseType either code or token
     * @param clientId     valid id of a client
     * @param redirectUri  a uri that the client is allowed to redirect to
     * @param scope        a string set of scopes
     * @return an error if anything is wrong with the aforementioned parameters, otherwise null
     */
    public static Response validateRequest(final EntityManager em,
                                           final String responseType, final String clientId, final String redirectUri, final String scope) {
        // verify all the query parameters are passed
        if (isBlank(clientId)) {
            return badRequest("The 'client_id' query parameter is required.");
        }

        // first look up the Client by the client identifier
        final Client client = QueryUtil.getClient(em, clientId);
        if (client == null) {
            return badRequest("Specified 'client_id' not found.");
        }

        if (isBlank(redirectUri) || isBlank(responseType)) {
            return badRequest(client, "'redirect_uri' and 'response_type' query parameters are required.");
        }

        if (!ResponseType.code.name().equals(responseType) &&
                !ResponseType.token.name().equals(responseType)) {
            return badRequest(client, "Invalid response type. Must be one of 'token' or 'code'");
        }

        // verify redirect URL is a proper redirect URL
        final URI toRedirect;
        try {
            toRedirect = new URI(redirectUri);
        } catch (Exception e) {
            return badRequest(client, "Invalid redirect URI: " + e.getMessage());
        }

        // verify the redirect uri is in the list of the client's allowed redirect uris
        boolean validRedirect = false;
        for (final String uri : client.getUris()) {
            try {
                final URI clientUri = new URI(uri);
                // scheme, host, and port must match
                if (UriUtil.partialMatch(clientUri, toRedirect)) {
                    validRedirect = true;
                    break;
                }
            } catch (Exception e) {
                return badRequest(client, "The client has an invalid redirect URI registered: " + uri + "; " + e.getMessage());
            }
        }
        if (!validRedirect) {
            return badRequest(client, "The redirect URI " + toRedirect.toString() + " is not allowed for this client.");
        }

        if (ResponseType.token.name().equals(responseType)) {
            if (!client.getFlows().contains(GrantFlow.IMPLICIT)) {
                return badRequest(client, "This client does not support the implicit grant flow.");
            }
        }

        if (ResponseType.code.name().equals(responseType)) {
            if (!client.getFlows().contains(GrantFlow.CODE)) {
                return badRequest(client, "This client does not support the code grant flow.");
            }
        }

        final Set<String> scopes = parseScope(scope);

        // verify all the requested scopes are available to the client
        if (scopes != null && !scopes.isEmpty()) {
            final Set<String> scopeNames = client.getScopes().stream()
                    .map(ClientScope::getScope)
                    .map(Scope::getName)
                    .collect(Collectors.toSet());

            if (!scopeNames.containsAll(scopes)) {
                final String joinedScopes = scopes.stream()
                        .filter((s) -> !scopeNames.contains(s))
                        .collect(Collectors.joining(", "));
                return badRequest(String.format("The following scopes are requested but not allowed for this client: %s", joinedScopes));
            }
        }

        return null;
    }

    /**
     * Helper function to generate an error template with a string error
     *
     * @param issue indicates what the problem with the request is
     * @return error page
     */
    public static Response badRequest(final Client client, final String issue) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new Viewable("/templates/ClientError", new ClientErrorModel(client, issue)))
                .build();
    }

    public static Response badRequest(final String issue) {
        return badRequest(null, issue);
    }
}
