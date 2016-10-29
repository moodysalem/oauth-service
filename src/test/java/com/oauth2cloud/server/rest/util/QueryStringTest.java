package com.oauth2cloud.server.rest.util;

import org.testng.annotations.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.testng.Assert.*;

public class QueryStringTest {

    @Test
    public void testQueryString() {
        final MultivaluedMap<String,String> testMap = new MultivaluedHashMap<>();

        assertEquals(QueryString.mapToQueryString(testMap), "");
    }
}