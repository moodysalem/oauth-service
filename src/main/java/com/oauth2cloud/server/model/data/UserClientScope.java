package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.AcceptedScope;
import com.oauth2cloud.server.model.db.ClientScope;

public class UserClientScope {
    private final ClientScope clientScope;
    private final AcceptedScope acceptedScope;

    public UserClientScope(ClientScope clientScope, AcceptedScope acceptedScope) {
        this.clientScope = clientScope;
        this.acceptedScope = acceptedScope;
    }

    public ClientScope getClientScope() {
        return clientScope;
    }

    public AcceptedScope getAcceptedScope() {
        return acceptedScope;
    }

    public boolean isAccepted() {
        return acceptedScope != null;
    }
}
