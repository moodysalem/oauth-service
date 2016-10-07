package com.oauth2cloud.server.rest.endpoints.oauth;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import java.util.logging.Logger;

abstract class BaseResource {
    static final Logger LOG = Logger.getLogger(BaseResource.class.getName());

    @Context
    protected ContainerRequestContext req;

    @Inject
    protected EntityManager em;

    CriteriaBuilder cb;

    @PostConstruct
    public void init() {
        cb = em.getCriteriaBuilder();
    }
}
