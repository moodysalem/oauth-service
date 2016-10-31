package com.oauth2cloud.server.model.db;

import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;
import com.oauth2cloud.server.hibernate.validate.NoSpaces;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import java.util.Objects;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Embeddable
public class ClientCredentials {
    public static ClientCredentials random(final int length) {
        return new ClientCredentials(randomAlphanumeric(length), randomAlphanumeric(length));
    }

    public ClientCredentials() {
    }

    public ClientCredentials(String id, String secret) {
        this.id = id;
        this.secret = secret;
    }

    @NotBlank
    @NoSpaces
    @Column(name = "id")
    @Convert(converter = EncryptedStringConverter.class)
    private String id;

    @NotBlank
    @NoSpaces
    @Column(name = "secret")
    @Convert(converter = EncryptedStringConverter.class)
    private String secret;

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientCredentials that = (ClientCredentials) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getSecret(), that.getSecret());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSecret());
    }
}
