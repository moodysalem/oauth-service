package com.oauth2cloud.server.rest.filter;

import com.oauth2cloud.server.model.Application_;
import com.oauth2cloud.server.model.Client_;
import com.oauth2cloud.server.model.Token_;
import com.oauth2cloud.server.model.db.Token;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Provides the request with the token in the TOKEN key by reading any bearer tokens from the authorization header
 */
@Provider
public class AuthorizationHeaderTokenFeature implements DynamicFeature {
    public static final String TOKEN = "TOKEN";

    public static final String BEARER = "bearer ";
    public static final UUID APPLICATION_ID = UUID.fromString("9966e7e3-ac4f-4d8e-9710-2971450cb504");
    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Priority(Priorities.AUTHENTICATION)
    public static class TokenFilter implements ContainerRequestFilter {
        @Inject
        private EntityManager em;

        @Override
        public void filter(ContainerRequestContext containerRequestContext) throws IOException {
            final CriteriaBuilder cb = em.getCriteriaBuilder();
            final String auth = containerRequestContext.getHeaderString(AUTHORIZATION_HEADER);
            if (auth != null && auth.toLowerCase().startsWith(BEARER)) {
                final String token = auth.substring(BEARER.length());
                if (token.length() > 0) {
                    final CriteriaQuery<Token> cq = cb.createQuery(Token.class);
                    final Root<Token> tokenRoot = cq.from(Token.class);
                    final List<Token> tks = em.createQuery(cq.select(tokenRoot).where(
                            cb.equal(tokenRoot.get(Token_.token), token),
                            cb.greaterThan(tokenRoot.get(Token_.expires), new Date()),
                            cb.equal(tokenRoot.get(Token_.type), Token.Type.ACCESS),
                            cb.equal(tokenRoot.join(Token_.client).join(Client_.application)
                                    .get(Application_.id), APPLICATION_ID)
                    )).getResultList();

                    if (tks.size() == 1) {
                        containerRequestContext.setProperty(TOKEN, tks.get(0));
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
            featureContext.register(TokenFilter.class);
        }
    }
}
