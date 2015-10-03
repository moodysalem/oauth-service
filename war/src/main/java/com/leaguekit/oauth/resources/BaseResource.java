package com.leaguekit.oauth.resources;

import com.leaguekit.jaxrs.lib.exceptions.RequestProcessingException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

public class BaseResource {

    protected Logger LOG = Logger.getLogger(BaseResource.class.getName());

    @Context
    HttpServletRequest req;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    private EntityTransaction etx;

    //idempotent initialization method
    @PostConstruct
    public void init() {
        cb = cb == null ? em.getCriteriaBuilder() : cb;
    }

    protected void beginTransaction() {
        if (etx != null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Nested Transactions Opened");
        }
        LOG.info("Beginning transaction");
        etx = em.getTransaction();
        etx.begin();
    }

    protected void commit() {
        if (etx == null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Transaction committed while not open");
        }
        LOG.info("Committing transaction");
        etx.commit();
        etx = null;
    }

    protected void rollback() {
        if (etx != null) {
            LOG.info("Rolling back transaction");
            etx.rollback();
            etx = null;
        }
    }

}
