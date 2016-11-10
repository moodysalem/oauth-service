package com.oauth2cloud.server.rest.endpoints.api.base;

import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.model.db.OAuthVersionedEntity;
import com.oauth2cloud.server.rest.filter.TokenFilter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

public abstract class VersionedEntityResource<T extends OAuthVersionedEntity> extends com.moodysalem.jaxrs.lib.resources.VersionedEntityResource<T> {
    @Inject
    protected EntityManager em;

    @Context
    protected ContainerRequestContext request;

    protected CriteriaBuilder cb;

    @PostConstruct
    protected void initCb() {
        cb = em.getCriteriaBuilder();
    }

    public User getUser() {
        return TokenFilter.getUser(request);
    }

    public void requireLoggedIn() {
        TokenFilter.requireLoggedIn(request);
    }

    public void requireScope(final String scope) {
        TokenFilter.requireScope(request, scope);
    }

    @Override
    public ContainerRequestContext getContainerRequestContext() {
        return request;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
