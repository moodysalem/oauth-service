package com.oauth2cloud.server.applications.oauth.models;

import com.oauth2cloud.server.hibernate.model.UserCode;

public class UserCodeEmailModel {
    private UserCode userCode;
    private String url;

    public UserCode getUserCode() {
        return userCode;
    }

    public void setUserCode(UserCode userCode) {
        this.userCode = userCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
