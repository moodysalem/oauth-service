package com.leaguekit.oauth;


import com.leaguekit.jaxrs.lib.BaseApplication;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("")
public class OAuthApplication extends BaseApplication {

    public OAuthApplication() {
        super();
        packages("com.leaguekit.oauth.resources");


    }

}