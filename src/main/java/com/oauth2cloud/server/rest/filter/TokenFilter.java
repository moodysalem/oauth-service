package com.oauth2cloud.server.rest.filter;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.util.QueryHelper;
import com.oauth2cloud.server.model.db.*;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Provides the request with the token in the TOKEN key by reading any bearer tokens from the authorization header
 */
@Provider
public class TokenFilter implements DynamicFeature {
    public static final String TOKEN = "TOKEN",
            BEARER = "bearer ",
            AUTHORIZATION_HEADER = "Authorization";

    public static final UUID APPLICATION_ID = UUID.fromString("9966e7e3-ac4f-4d8e-9710-2971450cb504");

    @Priority(Priorities.AUTHENTICATION)
    public static class ReadTokenFilter implements ContainerRequestFilter {
        @Inject
        private EntityManager em;

        @Override
        public void filter(ContainerRequestContext containerRequestContext) throws IOException {
            final CriteriaBuilder cb = em.getCriteriaBuilder();
            final String auth = containerRequestContext.getHeaderString(AUTHORIZATION_HEADER);
            if (auth != null && auth.toLowerCase().startsWith(BEARER)) {
                final String token = auth.substring(BEARER.length());
                if (token.length() > 0) {
                    final List<UserAccessToken> accessTokens = QueryHelper.query(
                            em,
                            UserAccessToken.class,
                            accessTokenPredicator -> cb.and(
                                    cb.equal(accessTokenPredicator.get(Token_.token), token),
                                    cb.greaterThan(accessTokenPredicator.get(Token_.expires), System.currentTimeMillis()),
                                    cb.equal(accessTokenPredicator.join(Token_.client).join(Client_.application)
                                            .get(Application_.id), APPLICATION_ID)
                            )
                    );

                    if (accessTokens.size() == 1) {
                        containerRequestContext.setProperty(TOKEN, accessTokens.get(0));
                    }
                }
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReadToken {
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        if (resourceInfo.getResourceMethod().isAnnotationPresent(ReadToken.class) ||
                resourceInfo.getResourceClass().isAnnotationPresent(ReadToken.class)) {
            featureContext.register(ReadTokenFilter.class);
        }
    }

    /**
     * Determine whether a user has a scope
     *
     * @param scope to check
     * @return true if user has scope
     */
    public static boolean hasScope(final ContainerRequestContext req, final String scope) {
        return isLoggedIn(req) && Stream.of(getToken(req).getScope().split(" ")).anyMatch(scope::equalsIgnoreCase);
    }

    /**
     * Check that the user has a specific scope
     *
     * @param scope to check
     */
    public static void requireScope(final ContainerRequestContext req, final String scope) {
        requireLoggedIn(req);
        if (!hasScope(req, scope)) {
            throw new RequestProcessingException(Response.Status.FORBIDDEN, String.format("'%s' scope is required for this resource.", scope));
        }
    }

    /**
     * Get the token out of the request
     *
     * @return token out of request
     */
    public static UserAccessToken getToken(final ContainerRequestContext req) {
        return (UserAccessToken) req.getProperty(TOKEN);
    }

    public static User getUser(final ContainerRequestContext req) {
        final UserToken tr = getToken(req);
        if (tr == null) {
            return null;
        }
        return tr.getUser();
    }

    public static boolean isLoggedIn(final ContainerRequestContext req) {
        return getUser(req) != null;
    }

    public static void requireLoggedIn(final ContainerRequestContext req) {
        if (!isLoggedIn(req)) {
            throw new RequestProcessingException(Response.Status.UNAUTHORIZED, "You must be logged in to access this resource.");
        }
    }
}
