package com.oauth2cloud.server.applications.admin.filter;

import com.oauth2cloud.server.hibernate.model.Token;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Provider
public class TokenFilter implements ContainerRequestFilter {

    public static final String BEARER = "bearer ";
    public static final String TOKEN = "TOKEN";
    public static final String CLIENT_ID = System.getProperty("CLIENT_ID");

    private static final Logger LOG = Logger.getLogger(TokenFilter.class.getName());

    public TokenFilter() {
        super();
        LOG.info("TokenFilter INSTANTIATED");
    }

    @Inject
    EntityManager em;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        String auth = containerRequestContext.getHeaderString("Authorization");
        if (auth != null && auth.toLowerCase().startsWith(BEARER)) {
            String token = auth.substring(BEARER.length());
            if (token.length() > 0) {
                CriteriaQuery<Token> cq = cb.createQuery(Token.class);
                Root<Token> rt = cq.from(Token.class);
                List<Token> tks = em.createQuery(cq.select(rt).where(
                    cb.equal(rt.get("token"), token),
                    cb.greaterThan(rt.get("expires"), new Date()),
                    cb.equal(rt.get("type"), Token.Type.ACCESS)
                )).getResultList();
                if (tks.size() == 1) {
                    containerRequestContext.setProperty(TOKEN, tks.get(0));
                }
            }
        }
    }
}
