package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.LoginCode;

/**
 * Used in the e-mail sent to log a user in
 */
public class LoginEmailModel {
    public LoginEmailModel(final LoginCode loginCode) {
        this.loginCode = loginCode;
    }

    private final LoginCode loginCode;

    public LoginCode getLoginCode() {
        return loginCode;
    }

    public String getFriendlyTtl() {
        final Integer ttl = loginCode.getClient().getLoginCodeTtl();
        if (ttl == null) {
            return "*never*";
        }

        if (ttl <= 60) {
            return String.format("%s second(s)", ttl);
        } else if (ttl <= 3600) {
            return String.format("%s minute(s)", ttl / 60);
        } else if (ttl <= 86400) {
            return String.format("%s hour(s)", ttl / 3600);
        } else if (ttl <= 604800) {
            return String.format("%s day(s)", ttl / 604800);
        } else {
            return String.format("%s week(s)", ttl / 4233600);
        }
    }
}
