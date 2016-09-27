package com.oauth2cloud.server.model.data;

import com.oauth2cloud.server.model.db.Client;
import com.oauth2cloud.server.model.db.LoginCode;

public class LoginEmailModel {
    private final Client client;
    private final LoginCode loginCode;

    public LoginEmailModel(final Client client, LoginCode loginCode) {
        this.client = client;
        this.loginCode = loginCode;
    }

    public Client getClient() {
        return client;
    }

    public LoginCode getLoginCode() {
        return loginCode;
    }
}
