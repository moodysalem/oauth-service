package com.oauth2cloud.server.rest.util;

import org.testng.annotations.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class QueryStringTest {

    @Test
    public void testQueryString() throws UnsupportedEncodingException {
        final MultivaluedMap<String, String> testMap = new MultivaluedHashMap<>();

        assertEquals(QueryString.mapToQueryString(testMap), "");

        testMap.putSingle("scope", "");
        assertEquals(QueryString.mapToQueryString(testMap), "scope=");

        testMap.put("scope", Arrays.asList("red", "", "blue"));
        assertEquals(QueryString.mapToQueryString(testMap), "scope=red&scope=&scope=blue");

        testMap.putSingle("hello", "blue car down");
        assertEquals(URLDecoder.decode(QueryString.mapToQueryString(testMap), "UTF-8"),
                "scope=red&scope=&scope=blue&hello=blue car down");
    }
}