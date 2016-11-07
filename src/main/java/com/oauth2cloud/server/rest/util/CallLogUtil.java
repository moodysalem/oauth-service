package com.oauth2cloud.server.rest.util;

import com.moodysalem.jaxrs.lib.resources.util.TXHelper;
import com.oauth2cloud.server.model.db.*;

import javax.persistence.EntityManager;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CallLogUtil {
    private static final Logger LOG = Logger.getLogger(CallLogUtil.class.getName());

    /**
     * Log an API_PATH call
     *
     * @param client      client making the call
     * @param application application making the call
     * @return CallLog object that is created
     */
    public static CallLog logCall(final EntityManager em,
                                  final Client client,
                                  final Application application,
                                  final ContainerRequestContext containerRequestContext) {
        if (client == null && application == null) {
            throw new NullPointerException();
        }

        final CallLog callLog;

        if (client != null) {
            final ClientCallLog clientCallLog = new ClientCallLog();
            clientCallLog.setClient(client);
            callLog = clientCallLog;
        } else {
            final ApplicationCallLog applicationCallLog = new ApplicationCallLog();
            applicationCallLog.setApplication(application);
            callLog = applicationCallLog;
        }

        final String forwardedIp = containerRequestContext.getHeaderString("X-Forwaded-For");
        if (forwardedIp != null) {
            callLog.setIp(forwardedIp);
        }

        callLog.setPath(containerRequestContext.getUriInfo().getPath());
        callLog.setMethod(containerRequestContext.getMethod());

        try {
            TXHelper.withinTransaction(em, () -> em.persist(callLog));
            return null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to log a call", e);
            return null;
        }
    }

    public static ClientCallLog logCall(final EntityManager em, final Client client, final ContainerRequestContext request) {
        final CallLog cl = logCall(em, client, null, request);
        if (cl != null && cl instanceof ClientCallLog) {
            return (ClientCallLog) cl;
        }
        return null;
    }

    public static ApplicationCallLog logCall(final EntityManager em, final Application application, final ContainerRequestContext request) {
        final CallLog al = logCall(em, null, application, request);
        if (al != null && al instanceof ApplicationCallLog) {
            return (ApplicationCallLog) al;
        }
        return null;
    }
}
