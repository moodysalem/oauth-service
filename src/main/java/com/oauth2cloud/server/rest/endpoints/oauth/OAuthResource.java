package com.oauth2cloud.server.rest.endpoints.oauth;

import com.oauth2cloud.server.hibernate.util.QueryUtil;
import com.oauth2cloud.server.model.data.ErrorModel;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;
import com.oauth2cloud.server.model.db.Token;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.email.Email;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.Message;
import javax.persistence.EntityManager;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class OAuthResource {
    protected Logger LOG = Logger.getLogger(OAuthResource.class.getName());

    public static final String COOKIE_NAME_PREFIX = "_AID_";
    public static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;
    private static final String FAILED_TO_SEND_E_MAIL_MESSAGE = "Failed to send e-mail message";

    protected static final String FROM_EMAIL = System.getProperty("SEND_EMAILS_FROM", "admin@oauth2cloud.com");

    @Context
    protected ContainerRequestContext containerRequestContext;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    @PostConstruct
    public void init() {
        cb = em.getCriteriaBuilder();
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
        LoginCookie lc = QueryUtil.getLoginCookie(em, secret, client);
        loginCookieLookupMap.put(client, lc);
        return lc;
    }


    /**
     * Helper function to generate an error template with a string error
     *
     * @param error indicates what the problem with the request is
     * @return error page
     */
    protected Response error(final String error) {
        return Response.status(400)
                .entity(new Viewable("/templates/Error", new ErrorModel(error)))
                .build();
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
        final StringWriter sw = new StringWriter();
        cfg.getTemplate(template).process(model, sw);
        return sw.toString();
    }


}
