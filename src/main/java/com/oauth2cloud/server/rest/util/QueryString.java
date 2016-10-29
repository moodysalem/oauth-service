package com.oauth2cloud.server.rest.util;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class QueryString {
    private static final String UTF_8 = "UTF-8";
    private static final Logger LOG = Logger.getLogger(QueryString.class.getName());
    private static final char AMPERSAND = '&', EQUALS = '=';

    /**
     * Helper function that converts a map to its query string representation. This is used when setting the fragment
     * in the response uri of a token grant flow
     *
     * @param map of parameters to generate the query string for
     * @return a query string style representation of the map
     */
    public static String mapToQueryString(final MultivaluedMap<String, String> map) {
        final StringBuilder sb = new StringBuilder();

        if (map != null) {
            for (final String key : map.keySet()) {
                for (final String value : map.get(key)) {
                    if (sb.length() > 0) {
                        sb.append(AMPERSAND);
                    }
                    try {
                        sb.append(URLEncoder.encode(key, UTF_8)).append(EQUALS).append(URLEncoder.encode(value, UTF_8));
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Failed to encode map", e);
                    }
                }
            }
        }

        return sb.toString();
    }
}
