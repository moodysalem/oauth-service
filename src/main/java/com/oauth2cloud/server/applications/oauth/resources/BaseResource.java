package com.oauth2cloud.server.applications.oauth.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.util.RandomStringUtil;
import com.oauth2cloud.server.applications.oauth.models.ErrorModel;
import com.oauth2cloud.server.hibernate.model.*;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseResource {
    public static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;
    // this is the minimum amount of time we should wait to validate a password
    // this serves two purposes - minimizing the threat of brute force attacks, and preventing timing attacks
    public static final long MINIMUM_LOGIN_TIME = 1000L;
    public static final String COOKIE_NAME_PREFIX = "_AID_";
    public static final Long FIVE_MINUTES = 1000L * 60L * 5L;
    private static final String FAILED_TO_SEND_E_MAIL_MESSAGE = "Failed to send e-mail message";

    protected static final String FROM_EMAIL = System.getProperty("SEND_EMAILS_FROM", "admin@oauth2cloud.com");

    protected Logger LOG = Logger.getLogger(BaseResource.class.getName());

    @Context
    protected ContainerRequestContext containerRequestContext;

    @Context
    ObjectMapper om;

    @Inject
    protected EntityManager em;

    protected CriteriaBuilder cb;

    private EntityTransaction etx;

    @PostConstruct
    public void init() {
        cb = cb == null ? em.getCriteriaBuilder() : cb;
    }

    /**
     * Get a token given the token string and the client it's for
     *
     * @param token  the token string
     * @param client the client it was issued to
     * @return the token or null if it doesn't exist or has expired
     */
    protected Token getToken(String token, Client client, Token.Type... types) {
        if (token == null) {
            return null;
        }
        CriteriaQuery<Token> tq = cb.createQuery(Token.class);
        Root<Token> t = tq.from(Token.class);

        Predicate p = cb.and(
            cb.equal(t.get("token"), token),
            t.get("type").in(types),
            cb.greaterThan(t.<Date>get("expires"), new Date())
        );
        if (client != null) {
            p = cb.and(p, cb.equal(t.get("client"), client));
        }

        List<Token> tkns = em.createQuery(tq.select(t).where(p)).getResultList();
        return tkns.size() == 1 ? tkns.get(0) : null;
    }

    /**
     * Create and persist a token
     *
     * @param type    the token's type
     * @param client  the client for which the token is being created
     * @param user    the to which the token is associated
     * @param expires when the token becomes invalid
     * @param scopes  the scopes for which the token is valid
     * @return a Token with the aforementioned properties
     */
    protected Token generateToken(Token.Type type, Client client, User user, Date expires, String redirectUri,
                                  List<AcceptedScope> scopes, Token refreshToken, List<ClientScope> clientScopes) {
        Token toReturn = new Token();
        toReturn.setClient(client);
        toReturn.setExpires(expires);
        toReturn.setUser(user);
        toReturn.setType(type);
        toReturn.setRedirectUri(redirectUri);
        toReturn.setRandomToken(64);
        toReturn.setAcceptedScopes(scopes);
        toReturn.setRefreshToken(refreshToken);
        try {
            beginTransaction();
            em.persist(toReturn);
            em.flush();
            commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create a token", e);
            rollback();
            return null;
        }
        return toReturn;
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

        if (Token.Type.CODE.equals(type) || Token.Type.PERMISSION.equals(type)) {
            milliseconds = FIVE_MINUTES;
        }

        return new Date(System.currentTimeMillis() + milliseconds);
    }

    private HashMap<String, Client> clientCache = new HashMap<>();

    /**
     * Get the client with a specific client ID
     *
     * @param clientId a client identifier
     * @return the Client corresponding to a client identifier
     */
    protected Client getClient(String clientId) {
        if (clientId == null) {
            return null;
        }
        if (clientCache.containsKey(clientId)) {
            return clientCache.get(clientId);
        }

        CriteriaQuery<Client> cq = cb.createQuery(Client.class);
        Root<Client> ct = cq.from(Client.class);
        cq.select(ct);
        cq.where(cb.equal(ct.get("identifier"), clientId));

        List<Client> cts = em.createQuery(cq).getResultList();
        Client c = (cts.size() != 1) ? null : cts.get(0);
        clientCache.put(clientId, c);
        return c;
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
     * Find the accepted scope or return null if it's not yet accepted
     *
     * @param user        that has accepted the scope
     * @param clientScope to look for
     * @return AcceptedScope for the user/clientScope
     */
    protected AcceptedScope findAcceptedScope(User user, ClientScope clientScope) {
        CriteriaQuery<AcceptedScope> cas = cb.createQuery(AcceptedScope.class);
        Root<AcceptedScope> ras = cas.from(AcceptedScope.class);
        List<AcceptedScope> las = em.createQuery(cas.select(ras).where(cb.and(
            cb.equal(ras.get("user"), user),
            cb.equal(ras.get("clientScope"), clientScope)
        ))).getResultList();
        if (las.size() == 1) {
            return las.get(0);
        }
        return null;
    }

    /**
     * Accept a scope for a user
     *
     * @param user        accepting the scope
     * @param clientScope that is being accepted
     * @return the AcceptedScope that is created/found for the user/clientscope
     */
    protected AcceptedScope acceptScope(User user, ClientScope clientScope) {
        AcceptedScope as = findAcceptedScope(user, clientScope);
        if (as != null) {
            return as;
        }
        as = new AcceptedScope();
        as.setUser(user);
        as.setClientScope(clientScope);

        try {
            beginTransaction();
            em.persist(as);
            em.flush();
            commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to accept a scope", e);
            rollback();
            return null;
        }
        return as;
    }

    /**
     * Get a list of client scopes limited to scopes with names in the scopes list
     *
     * @param client client to get the scopes for
     * @param scopes filter to scopes with these names (null if you want all scopes)
     * @return list of scopes filtered to scopes with the names passed
     */
    protected List<ClientScope> getScopes(Client client, List<String> scopes) {
        CriteriaQuery<ClientScope> cq = cb.createQuery(ClientScope.class);
        Root<ClientScope> rcs = cq.from(ClientScope.class);
        Predicate p = cb.and(cb.equal(rcs.get("client"), client), cb.equal(rcs.get("approved"), true));
        if (scopes != null && scopes.size() > 0) {
            p = cb.and(p, rcs.join("scope").get("name").in(scopes));
        }
        return em.createQuery(cq.select(rcs).where(p)).getResultList();
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
     * @param client
     * @return
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
        LoginCookie lc = getLoginCookie(secret, client);
        loginCookieLookupMap.put(client, lc);
        return lc;
    }

    /**
     * Look up a login cookie by the cookie value and the client (joined to application)
     *
     * @param secret secret of the cookie
     * @param client requesting client
     * @return LoginCookie for the secret and client
     */
    private LoginCookie getLoginCookie(String secret, Client client) {
        CriteriaQuery<LoginCookie> lc = cb.createQuery(LoginCookie.class);
        Root<LoginCookie> rlc = lc.from(LoginCookie.class);
        lc.select(rlc).where(
            cb.equal(rlc.get("secret"), secret),
            cb.greaterThan(rlc.<Date>get("expires"), new Date()),
            cb.equal(rlc.join("user").get("application"), client.getApplication())
        );
        List<LoginCookie> lcL = em.createQuery(lc).getResultList();
        return (lcL.size() == 1) ? lcL.get(0) : null;
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

    /**
     * Get the user associated with an e-mail and an application
     *
     * @param email       user e-mail address
     * @param application the application for which we're searching the user base
     * @return the User record
     */
    protected User getUser(String email, Application application) {
        CriteriaQuery<User> uq = cb.createQuery(User.class);
        Root<User> u = uq.from(User.class);

        List<User> users = em.createQuery(
            uq.select(u).where(
                cb.and(
                    cb.equal(u.get("application"), application),
                    cb.equal(u.get("email"), email)
                )
            )
        ).getResultList();

        if (users.size() != 1) {
            return null;
        }
        return users.get(0);
    }


    @PreDestroy
    public void cleanUp() {
        if (etx != null) {
            LOG.log(Level.SEVERE, "A transaction was not closed at the end of a request.");
            rollback();
        }
    }

    @Inject
    private Session mailSession;

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
            final MimeMessage m = new MimeMessage(mailSession);
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            Address fromAddress = new InternetAddress(FROM_EMAIL);
            m.setFrom(fromAddress);
            if (replyTo != null) {
                try {
                    m.setReplyTo(new Address[]{new InternetAddress(replyTo)});
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to set reply-to for e-mail", e);
                }
            }
            m.setSubject(subject);
            m.setContent(processTemplate(template, model), "text/html");
            new Thread(() -> {
                try {
                    Transport.send(m);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, FAILED_TO_SEND_E_MAIL_MESSAGE, e);
                }
            });
            Transport.send(m);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, FAILED_TO_SEND_E_MAIL_MESSAGE, e);
        }
    }

    protected boolean isEmpty(String check) {
        return check == null || check.trim().length() == 0;
    }

    /**
     * Get a UserCode based on the user code string
     *
     * @param code
     * @return
     */
    protected UserCode getCode(String code, UserCode.Type type, boolean includeUsed) {
        if (code == null) {
            return null;
        }
        CriteriaQuery<UserCode> pw = cb.createQuery(UserCode.class);
        Root<UserCode> rp = pw.from(UserCode.class);
        pw.select(rp).where(
            cb.equal(rp.get("code"), code),
            cb.greaterThan(rp.<Date>get("expires"), new Date()),
            cb.equal(rp.get("type"), type),
            // if we are including used, then use a predicate that will always be true
            includeUsed ? cb.isNotNull(rp.get("id")) : cb.equal(rp.get("used"), false)
        );
        List<UserCode> lp = em.createQuery(pw).getResultList();
        return lp.size() == 1 ? lp.get(0) : null;
    }


    /**
     * Make a UserCode
     *
     * @param user    user for which the code is created
     * @param referer the referer to which the user should be redirected after using the code
     * @param type    the type of code
     * @param expires when it expires
     * @return
     */
    protected UserCode makeCode(User user, String referer, UserCode.Type type, Date expires) {
        UserCode pw = new UserCode();
        pw.setExpires(expires);
        pw.setUser(user);
        pw.setCode(RandomStringUtil.randomAlphaNumeric(64));
        pw.setReferer(referer);
        pw.setType(type);
        pw.setExpires(expires);
        try {
            beginTransaction();
            em.persist(pw);
            commit();
        } catch (Exception e) {
            rollback();
            pw = null;
            LOG.log(Level.SEVERE, "Failed to create password reset code", e);
        }
        return pw;
    }


    /**
     * Process a mail template
     *
     * @param template template to process
     * @param model    object to inject into template
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    private String processTemplate(String template, Object model) throws IOException, TemplateException {
        StringWriter sw = new StringWriter();
        cfg.getTemplate(template).process(model, sw);
        return sw.toString();
    }

}
