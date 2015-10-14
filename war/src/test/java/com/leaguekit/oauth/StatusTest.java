package com.leaguekit.oauth;

import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.Assert.assertTrue;

public class StatusTest extends OAuthTest {

    @Test
    public void testStatus() {
        Response r = target("status").request().get();
        assertTrue(r.getStatus() == 200);
    }
}
