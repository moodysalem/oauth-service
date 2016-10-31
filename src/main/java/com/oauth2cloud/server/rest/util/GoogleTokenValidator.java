package com.oauth2cloud.server.rest.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth2cloud.server.model.db.ClientCredentials;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class GoogleTokenValidator {
    private static final WebTarget VALIDATE_TOKEN_ENDPOINT = ClientBuilder.newClient()
            .target("https://www.googleapis.com/oauth2/v3/tokeninfo");

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenInfoResponse {
        private final String aud, emailVerified, email;

        public TokenInfoResponse(
                @JsonProperty("aud") String aud,
                @JsonProperty("email_verified") String emailVerified,
                @JsonProperty("email") String email
        ) {
            this.aud = aud;
            this.emailVerified = emailVerified;
            this.email = email;
        }

        public String getAud() {
            return aud;
        }

        public String getEmail() {
            return email;
        }

        public boolean isVerified() {
            return "true".equalsIgnoreCase(emailVerified);
        }
    }

    public String getTokenEmail(final ClientCredentials googleCredentials, final String tokenString) {
        if (googleCredentials == null || isBlank(googleCredentials.getId()) || isBlank(googleCredentials.getSecret())) {
            throw new IllegalArgumentException("Missing google credentials");
        }

        if (isBlank(tokenString)) {
            throw new IllegalArgumentException("Invalid Google Token supplied.");
        }

        final Response tokenResponse = VALIDATE_TOKEN_ENDPOINT
                .queryParam("id_token", tokenString)
                .request(MediaType.APPLICATION_JSON).get();

        if (tokenResponse.getStatus() != 200) {
            throw new IllegalArgumentException("Invalid Google Token supplied.");
        }

        final TokenInfoResponse token = tokenResponse.readEntity(TokenInfoResponse.class);

        if (isBlank(token.getAud()) || !token.getAud().equals(googleCredentials.getId())) {
            throw new IllegalArgumentException("Token supplied is not for the correct client.");
        }

        if (!token.isVerified()) {
            throw new IllegalArgumentException("Google E-mail address is not yet verified.");
        }

        if (isBlank(token.getEmail())) {
            throw new IllegalArgumentException("Google token did not include e-mail information");
        }

        return token.getEmail();
    }
}
