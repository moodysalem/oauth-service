package com.oauth2cloud.server.rest.endpoints.oauth;

import com.moodysalem.jaxrs.lib.exceptionmappers.ErrorResponse;
import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCookie;
import com.oauth2cloud.server.rest.util.CookieUtil;
import com.oauth2cloud.server.rest.util.QueryUtil;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Level;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Api("oauth2")
@Path("logout")
@Produces("application/json")
public class LogoutResource extends BaseResource {

    @ApiOperation(
            value = "Log Out",
            notes = "This unsecured endpoint is used to expire any login cookies that a user may have for an application"
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "Returns a 204 if the client_id is valid"),
            @ApiResponse(code = 400, response = ErrorResponse.class, message = "Returns a 400 if the client_id is not provided")
    })
    @GET
    public Response logout(
            @ApiParam(value = "The ID of the client requesting to log the user out", required = true)
            @QueryParam("client_id") final String clientId
    ) {
        if (isBlank(clientId)) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "'client_id' is a required parameter");
        }

        final Client client = QueryUtil.getClient(em, clientId);

        if (client == null) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "Invalid 'client_id'");
        }

        final LoginCookie cookie = CookieUtil.getLoginCookie(em, req, client);
        if (cookie != null) {
            try {
                TXHelper.withinTransaction(em, () -> {
                    cookie.setExpires(new Date());
                    em.merge(cookie);
                });
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to expire login cookie", e);
                throw new RequestProcessingException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Failed to expire login cookie due to internal server error");
            }
        }

        return Response.noContent().build();
    }
}
