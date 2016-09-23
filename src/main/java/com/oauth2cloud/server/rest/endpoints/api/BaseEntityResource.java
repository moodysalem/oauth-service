package com.oauth2cloud.server.rest.endpoints.api;

import com.moodysalem.hibernate.model.BaseEntity;
import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.EntityResource;
import com.oauth2cloud.server.model.db.Token;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.rest.filter.AuthorizationHeaderTokenFeature;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("WeakerAccess")
public abstract class BaseEntityResource<T extends BaseEntity> extends EntityResource<T> {
    public static final String SORT = "sort";
    public static final String PIPE = "|";
    public static final String PERIOD = ".";
    public static final String START = "start";
    public static final String COUNT = "count";
    public static final String X_START = "X-Start";
    public static final String X_COUNT = "X-Count";
    public static final String X_TOTAL_COUNT = "X-Total-Count";

    @Context
    protected ContainerRequestContext req;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    @PostConstruct
    public void init() {
        cb = em.getCriteriaBuilder();
    }

    @Override
    protected ContainerRequestContext getContainerRequestContext() {
        return req;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public String getSortQueryParameterName() {
        return SORT;
    }

    @Override
    public String getSortInfoSeparator() {
        return PIPE;
    }

    @Override
    public String getSortPathSeparator() {
        return PERIOD;
    }

    @Override
    public int getMaxNumberOfSorts() {
        return 3;
    }

    @Override
    public String getStartQueryParameterName() {
        return START;
    }

    @Override
    public String getCountQueryParameterName() {
        return COUNT;
    }

    @Override
    public Integer getMaxPerPage() {
        return 100;
    }

    @Override
    public String getStartHeader() {
        return X_START;
    }

    @Override
    public String getCountHeader() {
        return X_COUNT;
    }

    @Override
    public String getTotalCountHeader() {
        return X_TOTAL_COUNT;
    }

    public Token getToken() {
        return (Token) req.getProperty(AuthorizationHeaderTokenFeature.TOKEN);
    }

    public User getUser() {
        Token tr = getToken();
        if (tr == null) {
            return null;
        }
        return tr.getUser();
    }

    protected void mustBeLoggedIn() {
        if (getUser() == null) {
            throw new RequestProcessingException(Response.Status.UNAUTHORIZED, "You must be logged in to access this resource.");
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public boolean isLoggedIn() {
        return getUser() != null;
    }

    private Set<String> scopes;

    /**
     * Determine whether a user has a scope
     *
     * @param scope
     * @return
     */
    protected boolean hasScope(String scope) {
        if (scopes == null) {
            scopes = new HashSet<>();
            if (getToken() != null) {
                String[] scps = getToken().getScope().split(" ");
                Collections.addAll(scopes, scps);
            }
        }
        return scopes.contains(scope);
    }

    /**
     * Check that the user has a specific scope
     *
     * @param scope
     */
    protected void checkScope(String scope) {
        if (!hasScope(scope)) {
            throw new RequestProcessingException(Response.Status.FORBIDDEN, String.format("'%s' scope is required for this resource.", scope));
        }
    }

}
