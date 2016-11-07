package com.oauth2cloud.server.rest.util;

import com.oauth2cloud.server.model.db.ClientCredentials;

public interface ProviderTokenValidator {
    String getTokenEmail(final ClientCredentials credentials, final String tokenString);
}
