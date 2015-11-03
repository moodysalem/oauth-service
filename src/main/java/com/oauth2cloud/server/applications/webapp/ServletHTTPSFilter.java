package com.oauth2cloud.server.applications.webapp;

import com.leaguekit.jaxrs.lib.filters.HTTPSFilter;
import com.oauth2cloud.server.applications.admin.APIApplication;
import com.oauth2cloud.server.applications.oauth.OAuthApplication;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServletHTTPSFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(ServletHTTPSFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String proto = req.getHeader(HTTPSFilter.PROTO_HEADER);
        if (proto != null && !proto.equalsIgnoreCase(HTTPSFilter.HTTPS)) {
            URI reqUri = null;
            try {
                reqUri = new URI(req.getRequestURL().toString());
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, "failed to parse URI in HTTPS filter", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            String path = reqUri.getPath() != null ? reqUri.getPath().toLowerCase() : "";
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (!path.startsWith(OAuthApplication.OAUTH) && !path.startsWith(APIApplication.API)) {
                resp.sendRedirect(
                    UriBuilder.fromUri(reqUri)
                        .scheme(HTTPSFilter.HTTPS)
                        .build()
                        .toString()
                );
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
