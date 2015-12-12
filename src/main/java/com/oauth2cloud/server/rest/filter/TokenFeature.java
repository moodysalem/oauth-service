package com.oauth2cloud.server.rest.filter;

import com.oauth2cloud.server.hibernate.model.Token;

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
import java.util.Date;
import java.util.List;

public class TokenFeature implements DynamicFeature {
    public static final String BEARER = "bearer ";
    public static final String TOKEN = "TOKEN";
    private static final long APPLICATION_ID = 1;

    @Provider
    @Priority(Priorities.AUTHENTICATION)
    public class TokenFilter implements ContainerRequestFilter {
        @Inject
        EntityManager em;

        @Override
        public void filter(ContainerRequestContext containerRequestContext) throws IOException {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            String auth = containerRequestContext.getHeaderString("Authorization");
            if (auth != null && auth.toLowerCase().startsWith(BEARER)) {
                String token = auth.substring(BEARER.length());
                if (token.length() > 0) {
                    CriteriaQuery<Token> cq = cb.createQuery(Token.class);
                    Root<Token> rt = cq.from(Token.class);
                    List<Token> tks = em.createQuery(cq.select(rt).where(
                            cb.equal(rt.get("token"), token),
                            cb.greaterThan(rt.get("expires"), new Date()),
                            cb.equal(rt.get("type"), Token.Type.ACCESS),
                            cb.equal(rt.join("client").join("application").get("id"), APPLICATION_ID)
                    )).getResultList();
                    if (tks.size() == 1) {
                        containerRequestContext.setProperty(TOKEN, tks.get(0));
                    }
                }
            }
        }
    }

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
