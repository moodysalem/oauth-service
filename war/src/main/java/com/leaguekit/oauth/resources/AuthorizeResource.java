package com.leaguekit.oauth.resources;

import com.leaguekit.oauth.model.Client;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.net.URL;
import java.util.List;

@Path("authorize")
public class AuthorizeResource extends BaseResource {

    public static class AuthorizationResponse {
    }


    @GET
    @Template(name = "/templates/Authorize")
    public Viewable auth(
        @QueryParam("response_type") String responseType,
        @QueryParam("client_id") String clientId,
        @QueryParam("redirect_uri") String redirectUri
    ) {
        if (clientId == null || redirectUri == null || responseType == null) {
            return new Viewable("/templates/Error", "Client ID, redirect URI, and response type are all required for this endpoint.");
        }
        URL toRedirect;
        try {
            toRedirect = new URL(redirectUri);
        } catch (Exception e) {
            return new Viewable("/templates/Error", "Invalid redirect URL: " + e.getMessage());
        }

        // first look up the Client by the client identifier
        Client c = getClient(clientId);
        if (c == null) {
            return new Viewable("/templates/Error", "Client ID not found.");
        }
        
        return new Viewable("/templates/Authorize");
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
