package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;
import com.oauth2cloud.server.model.db.Token;
import com.oauth2cloud.server.hibernate.util.OldQueryHelper;
import com.oauth2cloud.server.model.data.ErrorModel;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.email.Email;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.Message;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OAuthResource {
    public static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;
    public static final String COOKIE_NAME_PREFIX = "_AID_";
    public static final Long FIVE_MINUTES = 1000L * 60L * 5L;
    private static final String FAILED_TO_SEND_E_MAIL_MESSAGE = "Failed to send e-mail message";

    protected static final String FROM_EMAIL = System.getProperty("SEND_EMAILS_FROM", "admin@oauth2cloud.com");

    protected Logger LOG = Logger.getLogger(OAuthResource.class.getName());

    @Context
    protected ContainerRequestContext containerRequestContext;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    private EntityTransaction etx;

    @PostConstruct
    public void init() {
        cb = cb == null ? em.getCriteriaBuilder() : cb;
    }

    /**
     * Helper function to calculate when a token should expire based on the client's TTL
     *
     * @param client for which the token is being generated
     * @return when the token should expire
     */
    protected Date getExpires(Client client, Token.Type type) {
        Long milliseconds = client.getTokenTtl() * 1000L;

        if (Token.Type.REFRESH.equals(type)) {
            if (client.getRefreshTokenTtl() == null) {
                throw new IllegalArgumentException();
            }
            milliseconds = client.getRefreshTokenTtl() * 1000L;
        }

        if (Token.Type.CODE.equals(type) || Token.Type.PERMISSION.equals(type) || Token.Type.TEMPORARY.equals(type)) {
            milliseconds = FIVE_MINUTES;
        }

        return new Date(System.currentTimeMillis() + milliseconds);
    }

    /**
     * Helper function that converts a map to its query string representation. This is used when setting the fragment
     * in the response URI of a token grant flow
     *
     * @param map of parameters to generate the query string for
     * @return a query string style representation of the map
     */
    protected String mapToQueryString(MultivaluedMap<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            for (String value : map.get(key)) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                try {
                    sb.append(URLEncoder.encode(key, "UTF-8")).append('=').append(URLEncoder.encode(value, "UTF-8"));
                } catch (Exception ignored) {
                    LOG.log(Level.SEVERE, "Failed to encode map", ignored);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Get the cookie given by the name
     *
     * @param name name of the cookie to look up
     * @return a Cookie
     */
    protected Cookie getCookie(String name) {
        return containerRequestContext.getCookies().get(name);
    }

    /**
     * Get the name of a cookie that a client should be using
     */
    protected String getCookieName(Client client) {
        return COOKIE_NAME_PREFIX + client.getApplication().getId();
    }

    /**
     * Get the Cookie object for a specific client
     *
     * @param client the client for which a cookie should be found
     * @return the Cookie corresponding to the client
     */
    protected Cookie getCookie(Client client) {
        if (client == null) {
            return null;
        }
        return getCookie(getCookieName(client));
    }

    private HashMap<Client, LoginCookie> loginCookieLookupMap;

    /**
     * Get the LoginCookie for a specific client
     *
     * @param client to find the login cookie for
     * @return the login cookie
     */
    protected LoginCookie getLoginCookie(Client client) {
        if (loginCookieLookupMap == null) {
            loginCookieLookupMap = new HashMap<>();
        } else {
            if (loginCookieLookupMap.containsKey(client)) {
                return loginCookieLookupMap.get(client);
            }
        }
        Cookie c = getCookie(client);
        if (c == null) {
            return null;
        }
        String secret = c.getValue();
        LoginCookie lc = OldQueryHelper.getLoginCookie(em, secret, client);
        loginCookieLookupMap.put(client, lc);
        return lc;
    }


    /**
     * Begin a hibernate transaction
     */
    protected void beginTransaction() {
        if (etx != null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Nested Transactions Opened");
        }
        LOG.info("Beginning transaction");
        etx = em.getTransaction();
        etx.begin();
    }

    /**
     * Commit the in-process transaction
     */
    protected void commit() {
        if (etx == null) {
            throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR, "Transaction committed while not open");
        }
        LOG.info("Committing transaction");
        etx.commit();
        etx = null;
    }

    /**
     * Rollback a transaction if one exists
     */
    protected void rollback() {
        if (etx != null) {
            if (etx.isActive()) {
                LOG.info("Rolling back transaction");
                etx.rollback();
            }
            etx = null;
        }
    }

    /**
     * Check that two URIs match enough per the OAuth2 spec
     *
     * @param one one uri to check
     * @param two uri to check against
     * @return true if the uris match well enough
     */
    protected boolean partialMatch(URI one, URI two) {
        boolean validParams = one != null && two != null;
        return validParams &&
                one.getScheme().equalsIgnoreCase(two.getScheme()) &&
                one.getHost().equalsIgnoreCase(two.getHost()) &&
                one.getPort() == two.getPort();
    }

    /**
     * Helper function to generate an error template with a string error
     *
     * @param error indicates what the problem with the request is
     * @return error page
     */
    protected Response error(String error) {
        ErrorModel em = new ErrorModel();
        em.setError(error);
        return Response.status(400).entity(new Viewable("/templates/Error", em)).build();
    }

    @Inject
    private Mailer mailer;

    @Inject
    private Configuration cfg;

    /**
     * Send an e-mail using the template in the applications templates.email package
     *
     * @param replyTo  who to send from
     * @param to       who to send to
     * @param subject  of the email
     * @param template to build the e-mail
     * @param model    object to pass into template
     */
    protected void sendEmail(String replyTo, String to, String subject, String template, Object model) {
        try {
            final Email email = new Email();
            email.setFromAddress("OAuth2Cloud Admin", FROM_EMAIL);
            email.setSubject(subject);
            email.addRecipient(to, to, Message.RecipientType.TO);
            if (replyTo != null) {
                email.setReplyToAddress(replyTo, replyTo);
            }
            email.setTextHTML(processTemplate(template, model));
            try {
                mailer.sendMail(email);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, FAILED_TO_SEND_E_MAIL_MESSAGE, e);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, FAILED_TO_SEND_E_MAIL_MESSAGE, e);
        }
    }

    /**
     * Process a mail template
     *
     * @param template template to process
     * @param model    object to inject into template
     * @return String of the processed template
     * @throws IOException
     * @throws TemplateException
     */
    private String processTemplate(String template, Object model) throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        cfg.getTemplate(template).process(model, sw);
        return sw.toString();
    }


}
