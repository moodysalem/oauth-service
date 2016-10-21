package com.oauth2cloud.server;

import org.codemonkey.simplejavamail.email.Email;

import java.util.List;

public interface SendsMail {
    Email lastSentEmail();

    List<Email> getSentEmails();
}
