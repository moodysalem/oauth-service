package com.oauth2cloud.server.model.db;

import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

@Embeddable
public class ClientCredentials {
    public ClientCredentials(String id, String secret) {
        this.id = id;
        this.secret = secret;
    }

    @NotEmpty
    @Column(name = "id")
    @Convert(converter = EncryptedStringConverter.class)
    private String id;

    @NotEmpty
    @Column(name = "secret")
    @Convert(converter = EncryptedStringConverter.class)
    private String secret;

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }
}
