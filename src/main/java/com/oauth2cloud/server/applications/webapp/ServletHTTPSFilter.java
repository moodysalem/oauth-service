package com.oauth2cloud.server.applications.webapp;

import com.leaguekit.jaxrs.lib.filters.HTTPSFilter;

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
        String proto = req.getHeader(HTTPSFilter.PROTO_HEADER);
        if (proto != null && !proto.equalsIgnoreCase(HTTPSFilter.HTTPS)) {
            HttpServletResponse resp = (HttpServletResponse) response;
            try {
                resp.sendRedirect(
                    UriBuilder.fromUri(new URI(req.getRequestURI()))
                        .scheme(HTTPSFilter.HTTPS)
                        .replaceQuery("")
                        .build()
                        .toString()
                );
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, "failed to parse URI in HTTPS filter", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
