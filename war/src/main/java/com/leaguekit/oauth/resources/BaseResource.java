package com.leaguekit.oauth.resources;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class BaseResource {

    @Inject
    private EntityManager em;

}
