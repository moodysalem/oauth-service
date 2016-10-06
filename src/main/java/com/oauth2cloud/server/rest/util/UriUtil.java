package com.oauth2cloud.server.rest.util;

public class UriUtil {
    /**
     * Check that two URIs match enough per the OAuth2 spec
     *
     * @param one one uri to check
     * @param two uri to check against
     * @return true if the uris match well enough
     */
    public static boolean partialMatch(final java.net.URI one, final java.net.URI two) {
        return one != null && two != null &&
                one.getScheme().equalsIgnoreCase(two.getScheme()) &&
                one.getHost().equalsIgnoreCase(two.getHost()) &&
                one.getPort() == two.getPort();
    }
}
