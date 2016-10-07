package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCode;
import com.oauth2cloud.server.rest.util.QueryString;

import javax.ws.rs.core.Form;

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
