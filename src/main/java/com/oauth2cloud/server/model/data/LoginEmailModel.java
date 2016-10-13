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
}
