package com.oauth2cloud.server.rest.util;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class QueryString {
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
                    if (!isBlank(value) && sb.length() > 0) {
                        sb.append('&');
                    }
                    try {
                        sb.append(URLEncoder.encode(key, "UTF-8")).append('=').append(URLEncoder.encode(value, "UTF-8"));
                    } catch (Exception e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to encode map", e);
                    }
                }
            }
        }

        return sb.toString();
    }
}
