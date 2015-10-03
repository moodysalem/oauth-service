package com.leaguekit.oauth.resources;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class BaseResource {

    @Context
    HttpServletRequest req;

    @Inject
    private EntityManager em;

}
