package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.Client;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URL;
import java.util.List;

@Path("authorize")
public class AuthorizeResource extends BaseResource {

    @GET
    public Viewable auth(
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri
    ) {
        // verify all the query parameters are passed
        if (clientId == null || redirectUri == null || responseType == null) {
            return error("Client ID, redirect URI, and response type are all required for this endpoint.");
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

        return new Viewable("/templates/Authorize");
    }

    private Viewable error(String error) {
        return new Viewable("/templates/Error", error);
    }

    private Client getClient(String clientId) {
        CriteriaQuery<Client> cq = cb.createQuery(Client.class);
        Root<Client> ct = cq.from(Client.class);
        cq.where(cb.equal(ct.get("identifier"), clientId));

        List<Client> cts = em.createQuery(cq).getResultList();
        if (cts.size() != 1) {
            return null;
        }

        return cts.get(0);
    }

}
