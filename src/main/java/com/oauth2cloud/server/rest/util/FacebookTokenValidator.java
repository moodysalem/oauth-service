package com.oauth2cloud.server.rest.util;

import com.oauth2cloud.server.model.db.ClientCredentials;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.types.User;

import java.util.Date;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class FacebookTokenValidator implements ProviderTokenValidator {
    public String getTokenEmail(final ClientCredentials credentials, final String tokenString) {
        if (credentials == null || isBlank(credentials.getId()) || isBlank(credentials.getSecret())) {
            throw new IllegalArgumentException("Missing facebook credentials");
        }

        if (isBlank(tokenString)) {
            throw new IllegalArgumentException("Invalid Facebook Token supplied.");
        }

        final FacebookClient.DebugTokenInfo info = new DefaultFacebookClient(Version.VERSION_2_8)
                .debugToken(tokenString);

        if (info == null ||
                !info.getAppId().equals(credentials.getId()) ||
                !info.getExpiresAt().after(new Date())) {
            throw new IllegalArgumentException("Invalid token");
        }

        if (info.getScopes() == null || info.getScopes().stream().noneMatch("email"::equalsIgnoreCase)) {
            throw new IllegalArgumentException("Facebook 'email' scope is required");
        }

        final User user = (new DefaultFacebookClient(tokenString, Version.VERSION_2_8))
                .fetchObject("me", User.class);

        if (user == null || isBlank(user.getEmail())) {
            throw new IllegalArgumentException("Token did not associate to a user with e-mail");
        }

        return user.getEmail();
    }
}
