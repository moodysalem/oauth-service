package com.leaguekit.oauth.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A CORS filter for JAX-RS
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    public static final int ACCESS_CONTROL_CACHE_SECONDS = 2592000;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {

        MultivaluedMap<String, Object> headers = containerResponseContext.getHeaders();
        headers.remove("X-Powered-By");

        // only if origin header is present do we slap on these origin headers
        if (containerRequestContext.getHeaderString("Origin") != null) {
            // these are always ok
            headers.putSingle("Access-Control-Allow-Origin", "*");
            headers.putSingle("Access-Control-Allow-Credentials", "true");
            headers.putSingle("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");

            // allow all the request headers
            String requestHeadersAllowed = containerRequestContext.getHeaderString("Access-Control-Request-Headers");
            if (requestHeadersAllowed != null) {
                headers.putSingle("Access-Control-Allow-Headers", requestHeadersAllowed);
            }

            // also expose any non-standard response headers
            Set<String> exposedHeaderSet = new HashSet<>();
            exposedHeaderSet.addAll(
                containerResponseContext
                    .getHeaders()
                    .keySet()
                    .stream()
                    .filter((e) -> e.startsWith("X-"))
                    .map(String::trim)
                    .collect(Collectors.toSet())
            );

            if (exposedHeaderSet.size() > 0) {
                // collect the set into a string
                String exposedHeaders = exposedHeaderSet.stream().collect(Collectors.joining(", "));
                // and put it back in the map
                headers.putSingle("Access-Control-Expose-Headers", exposedHeaders);
            }

            // allow browser to cache this forever
            headers.putSingle("Access-Control-Max-Age", ACCESS_CONTROL_CACHE_SECONDS);
        }

    }

}
