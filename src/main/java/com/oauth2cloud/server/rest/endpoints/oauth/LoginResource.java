package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.filters.CORSFilter;
import com.moodysalem.jaxrs.lib.resources.util.QueryHelper;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.api.LoginErrorCode;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.data.PermissionsModel;
import com.oauth2cloud.server.model.data.UserClientScope;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.rest.filter.NoXFrameOptionsFeature;
import com.oauth2cloud.server.rest.util.OAuthUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.oauth2cloud.server.rest.util.CookieUtil.getNewCookie;
import static com.oauth2cloud.server.rest.util.QueryUtil.expectOne;

@Path("login/{code}")
@Produces(MediaType.TEXT_HTML)
@CORSFilter.Skip
@NoXFrameOptionsFeature.NoXFrame
public class LoginResource extends BaseResource {
    private static final String SCOPE_PREFIX = "SCOPE-";

    private static class Validation {
        private final Response error;
        private final LoginCode loginCode;

        public Validation(Response error, LoginCode loginCode) {
            this.error = error;
            this.loginCode = loginCode;
        }

        public Response getError() {
            return error;
        }

        public LoginCode getLoginCode() {
            return loginCode;
        }
    }

    private Validation commonValidation(final String code) {
        final LoginCode loginCode = QueryUtil.findLoginCode(em, code);

        if (loginCode == null) {
            return new Validation(OAuthUtil.badRequest("Invalid login code!"), null);
        }

        if (loginCode.getUsed()) {
            return new Validation(backToLogin(loginCode, LoginErrorCode.login_code_used), loginCode);
        }

        if (loginCode.getExpires() != null && loginCode.getExpires().before(new Date())) {
            return new Validation(backToLogin(loginCode, LoginErrorCode.login_code_expired), loginCode);
        }

        return new Validation(null, loginCode);
    }

    @GET
    public Response doLogin(@PathParam("code") final String code) {
        final Validation v = commonValidation(code);
        if (v.getError() != null) {
            return v.getError();
        }

        final LoginCode loginCode = v.getLoginCode();

        final Set<UserClientScope> userClientScopes = getUserClientScopes(loginCode);

        if (userClientScopes.isEmpty() && !loginCode.getClient().isShowPromptNoScopes()) {
            return acceptPermissions(code, "ok", new MultivaluedHashMap<>());
        }

        return Response.ok(
                new Viewable("/templates/Permissions.ftl", new PermissionsModel(loginCode, userClientScopes))
        ).build();
    }

    @POST
    public Response acceptPermissions(
            @PathParam("code") final String code,
            @FormParam("action") final String action,
            final MultivaluedMap<String, String> form
    ) {
        final Validation v = commonValidation(code);
        if (v.getError() != null) {
            return v.getError();
        }
        final LoginCode loginCode = v.getLoginCode();

        if ("cancel".equals(action)) {
            useCode(loginCode);
            return backToLogin(loginCode, LoginErrorCode.permission_denied);
        }

        // get all the scopes that were checked
        final Set<UUID> acceptedClientScopeIds = form.keySet().stream()
                .filter(key -> key.startsWith(SCOPE_PREFIX))
                .filter(key -> "on".equals(form.getFirst(key)))
                .map(key -> key.substring(SCOPE_PREFIX.length()))
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        // accept each of the scopes
        final Set<AcceptedScope> acceptedScopes = QueryUtil
                .getScopes(em, loginCode.getClient(), OAuthUtil.parseScope(loginCode.getScope()))
                .stream()
                .filter(clientScope ->
                        // they explicitly accepted it
                        acceptedClientScopeIds.contains(clientScope.getId()) ||
                                // or not asking
                                !ClientScope.Priority.ASK.equals(clientScope.getPriority()))
                .map(clientScope -> acceptScope(loginCode.getUser(), clientScope))
                .collect(Collectors.toSet());

        final ResponseType type = loginCode.getResponseType();

        // create a token
        final UserToken token;
        if (type.equals(ResponseType.token)) {
            token = new UserAccessToken();
        } else {
            token = new UserAccessCode();
        }
        token.setClient(loginCode.getClient());
        token.setUser(loginCode.getUser());
        token.setRedirectUri(loginCode.getRedirectUri());
        token.setAcceptedScopes(acceptedScopes);
        token.setLoginCode(loginCode);

        // save the token
        final UserToken saved;
        try {
            saved = TXHelper.withinTransaction(em, () -> em.merge(token));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create token", e);
            return backToLogin(loginCode, LoginErrorCode.internal_error);
        }

        try {
            useCode(loginCode);
            return getRedirectResponse(loginCode, saved);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to complete login", e);
            return backToLogin(loginCode, LoginErrorCode.internal_error);
        }
    }


    /**
     * Does the appropriate redirect based on the passed redirect URI and the token, always appending the state
     *
     * @param token access or code token that was generated, depending on the type
     */
    private Response getRedirectResponse(
            final LoginCode loginCode,
            final UserToken token
    ) {
        final UriBuilder toRedirect = UriBuilder.fromUri(loginCode.getRedirectUri());

        if (token instanceof UserAccessToken) {
            toRedirect.fragment(TokenResponse.from(token).toFragment(loginCode.getState()));
        }

        if (token instanceof UserAccessCode) {
            toRedirect.queryParam("code", token.getToken());
            if (loginCode.getState() != null) {
                toRedirect.queryParam("state", loginCode.getState());
            }
        }

        final Response.ResponseBuilder builder = Response.status(Response.Status.FOUND)
                .location(toRedirect.build());

        if (loginCode.getRememberMe()) {
            builder.cookie(getNewCookie(em, token.getUser(), token.getClient()));
        }

        return builder.build();
    }


    /**
     * Mark a login code used
     *
     * @param loginCode to mark used
     */
    private void useCode(final LoginCode loginCode) {
        try {
            TXHelper.withinTransaction(em, () -> {
                loginCode.setUsed(true);
                em.merge(loginCode);
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to use a login code", e);
        }
    }


    /**
     * Get the scopes that we need to show or ask the user about
     * REQUIRED_HIDDEN permissions are not shown because the client always has those permissions when the user logs in
     * REQUIRED permissions are shown but the user does not have an option if they wish to log in to the client
     * ASK permissions are shown and the user has the option not to grant them to the service
     *
     * @return list of scopes we should ask for
     */
    public Set<UserClientScope> getUserClientScopes(final LoginCode loginCode) {
        final Set<String> scopes = OAuthUtil.parseScope(loginCode.getScope());
        // get all the client scopes for this client
        final List<ClientScope> clientScopes = QueryHelper.query(em, ClientScope.class, clientScope ->
                cb.and(
                        // since a client will always get these scopes, we don't show them
                        cb.notEqual(clientScope.get(ClientScope_.priority), ClientScope.Priority.REQUIRED_HIDDEN),
                        // for this client
                        cb.equal(clientScope.join(ClientScope_.client), loginCode.getClient()),
                        // specific scope names requested
                        (scopes != null && !scopes.isEmpty()) ?
                                clientScope.join(ClientScope_.scope).get(Scope_.name).in(scopes) :
                                cb.and()
                )
        );

        // group accepted scopes by the client scope ID
        final Map<UUID, AcceptedScope> accepted = (
                clientScopes.isEmpty() ?
                        Collections.<AcceptedScope>emptyList() :
                        QueryHelper.query(em, AcceptedScope.class, acceptedScope ->
                                cb.and(
                                        // for the user
                                        cb.equal(acceptedScope.get(AcceptedScope_.user), loginCode.getUser()),
                                        // in the list of scopes
                                        acceptedScope.get(AcceptedScope_.clientScope).in(clientScopes)
                                )
                        )
        ).stream()
                .collect(
                        Collectors.toMap(
                                acceptedScope -> acceptedScope.getClientScope().getId(),
                                Function.identity()
                        )
                );

        return clientScopes.stream()
                .map(clientScope -> new UserClientScope(clientScope, accepted.get(clientScope.getId())))
                .collect(Collectors.toSet());
    }

    /**
     * Accept a scope for a user
     *
     * @param user        accepting the scope
     * @param clientScope that is being accepted
     * @return the AcceptedScope that is created/found for the user/clientscope
     */
    private AcceptedScope acceptScope(final User user, final ClientScope clientScope) {
        final AcceptedScope existing = findAcceptedScope(user, clientScope);
        if (existing != null) {
            return existing;
        }

        final AcceptedScope nu = new AcceptedScope();
        nu.setUser(user);
        nu.setClientScope(clientScope);

        try {
            return TXHelper.withinTransaction(em, () -> em.merge(nu));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to accept scope", e);
            return null;
        }
    }

    /**
     * Find the accepted scope or return null if it's not yet accepted
     *
     * @param user        that has accepted the scope
     * @param clientScope to look for
     * @return AcceptedScope for the user/clientScope
     */
    private AcceptedScope findAcceptedScope(final User user, final ClientScope clientScope) {
        return expectOne(
                QueryHelper.query(em, AcceptedScope.class, root ->
                        cb.and(
                                cb.equal(root.get(AcceptedScope_.user), user),
                                cb.equal(root.get(AcceptedScope_.clientScope), clientScope)
                        )
                )
        );
    }


    /**
     * Kick a user back to login with an error code
     *
     * @param loginCode      used to generate the url
     * @param loginErrorCode inserted into the url so the login page knows to show an error
     * @return response
     */
    private Response backToLogin(final LoginCode loginCode, final LoginErrorCode loginErrorCode) {
        final UriBuilder ub = UriBuilder.fromUri(loginCode.getBaseUri())
                .path("authorize")
                .queryParam("client_id", loginCode.getClient().getCredentials().getId())
                .queryParam("response_type", loginCode.getResponseType())
                .queryParam("redirect_uri", loginCode.getRedirectUri());

        if (loginCode.getScope() != null) {
            ub.queryParam("scope", loginCode.getScope());
        }
        if (loginCode.getState() != null) {
            ub.queryParam("state", loginCode.getState());
        }
        if (loginErrorCode != null) {
            ub.queryParam("error_code", loginErrorCode.name());
        }

        return Response.status(Response.Status.FOUND).location(ub.build()).build();
    }
}
