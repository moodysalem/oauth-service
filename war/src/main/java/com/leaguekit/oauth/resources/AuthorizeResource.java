package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.Client;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

@Path("authorize")
@Produces(MediaType.TEXT_HTML)
public class AuthorizeResource extends BaseResource {

    public static final String TOKEN = "token";
    public static final String CODE = "code";

    private Viewable validateParameters(String responseType, String clientId, String redirectUri) {
        // verify all the query parameters are passed
        if (clientId == null || redirectUri == null || responseType == null) {
            return error("Client ID, redirect URI, and response type are all required for this endpoint.");
        }
        if (!TOKEN.equalsIgnoreCase(responseType) && !CODE.equalsIgnoreCase(responseType)) {
            return error("Invalid response type. Must be one of 'token' or 'code'");
        }

        // verify redirect URL is a proper redirect URL
        URI toRedirect;
        try {
            toRedirect = new URI(redirectUri);
        } catch (Exception e) {
            return error("Invalid redirect URL: " + e.getMessage());
        }

        // first look up the Client by the client identifier
        Client c = getClient(clientId);
        if (c == null) {
            return error("Client ID not found.");
        }

        // verify the redirect uri is in the list of the client's allowed redirect uris
        boolean validRedirect = false;
        for (String uri : c.getUris()) {
            try {
                URI cUri = new URI(uri);
                // scheme, host, and port must match
                if (cUri.getScheme().equalsIgnoreCase(toRedirect.getScheme()) &&
                    cUri.getHost().equalsIgnoreCase(toRedirect.getHost()) &&
                    cUri.getPort() == toRedirect.getPort()) {
                    validRedirect = true;
                    break;
                }
            } catch (Exception e) {
                return error("The client has an invalid redirect URI registered: " + uri + "; " + e.getMessage());
            }
        }
        if (!validRedirect) {
            return error("The redirect URI " + toRedirect.toString() + " is not allowed for this client.");
        }

        if (TOKEN.equalsIgnoreCase(responseType)) {
            if (!c.getFlows().contains(Client.GrantFlow.IMPLICIT)) {
                return error("This client does not support the implicit grant flow.");
            }
        }
        if (CODE.equalsIgnoreCase(responseType)) {
            if (!c.getFlows().contains(Client.GrantFlow.CODE)) {
                return error("This client does not support the code grant flow.");
            }
        }

        return null;
    }

    @GET
    public Viewable auth(
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri,
        @QueryParam("state") String state
    ) {
        Viewable error = validateParameters(responseType, clientId, redirectUri);
        if (error != null) {
            return error;
        }

        return new Viewable("/templates/Authorize");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Viewable login(
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri,
        @QueryParam("state") String state,
        @FormParam("email") String email,
        @FormParam("password") String password
    ) {
        // validate the client id stuff again
        Viewable error = validateParameters(responseType, clientId, redirectUri);
        if (error != null) {
            return error;
        }

        return new Viewable("/templates/Authorize");
    }

    private Viewable error(String error) {
        return new Viewable("/templates/Error", error);
    }

    private HashMap<String, Client> clientCache = new HashMap<>();

    private Client getClient(String clientId) {
        if (clientCache.containsKey(clientId)) {
            return clientCache.get(clientId);
        }

        CriteriaQuery<Client> cq = cb.createQuery(Client.class);
        Root<Client> ct = cq.from(Client.class);
        cq.where(cb.equal(ct.get("identifier"), clientId));

        List<Client> cts = em.createQuery(cq).getResultList();
        Client c = (cts.size() != 1) ? null : cts.get(0);
        clientCache.put(clientId, c);
        return c;
    }

}
