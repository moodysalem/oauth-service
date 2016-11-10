package com.oauth2cloud.server.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Entity
@Table(name = "tokens")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Token extends OAuthVersionedEntity {
    @Size(max = 96, min = 32)
    @Column(name = "token", updatable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String token;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "client_id", updatable = false)
    private Client client;

    @NotNull
    @Column(name = "expires")
    private Long expires;

    /**
     * @return a space delimited list of scope names, for approved client scopes that are active
     */
    public abstract String getScope();

    /**
     * @return the number of seconds remaining on the token
     */
    public Long getExpiresIn() {
        return (expires - System.currentTimeMillis()) / 1000L;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpires() {
        return expires == null ? null : new Date(expires);
    }

    public void setExpires(Date expires) {
        this.expires = expires == null ? null : expires.getTime();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public abstract Long getTtl(final Client client);

    private void setExpiresFromClient(final Client client) {
        if (client != null) {
            setExpires(new Date(System.currentTimeMillis() + getTtl(client)));
        }
    }

    @PreUpdate
    @PrePersist
    public void generateToken() {
        if (getToken() == null) {
            setToken(randomAlphanumeric(96));
        }
        if (getExpires() == null) {
            setExpiresFromClient(getClient());
        }
    }

}
