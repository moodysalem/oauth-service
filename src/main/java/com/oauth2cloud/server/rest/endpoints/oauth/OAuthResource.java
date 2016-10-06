package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.model.data.ErrorModel;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

public abstract class OAuthResource {
    static final Logger LOG = Logger.getLogger(OAuthResource.class.getName());

    public static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;

    @Context
    protected ContainerRequestContext req;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    @PostConstruct
    public void init() {
        cb = em.getCriteriaBuilder();
    }

    /**
     * Helper function to generate an error template with a string error
     *
     * @param error indicates what the problem with the request is
     * @return error page
     */
    protected Response error(final String error) {
        return Response.status(400)
                .entity(new Viewable("/templates/Error", new ErrorModel(error)))
                .build();
    }
}
