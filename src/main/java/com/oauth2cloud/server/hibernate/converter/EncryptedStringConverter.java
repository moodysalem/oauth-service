package com.oauth2cloud.server.hibernate.converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.security.Key;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows storing encrypted data in the database
 * <p>
 * Apply it to the fields that should be encrypted by the encryption secret passed in to the application via the
 * ENCRYPTION_SECRET environment variable
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final Logger LOG = Logger.getLogger(EncryptedStringConverter.class.getName());

    private static final String ENCRYPTION_SECRET = System.getProperty("ENCRYPTION_SECRET");
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY_STRING = ENCRYPTION_SECRET.getBytes();
    private static final Key KEY = new SecretKeySpec(KEY_STRING, "AES");

    private static final Cipher ENCRYPTION_CIPHER;
    private static final Cipher DECRYPTION_CIPHER;
    private static final String UTF_8 = "UTF-8";

    static {
        Cipher x = null, y = null;

        try {
            x = Cipher.getInstance(ALGORITHM);
            x.init(Cipher.ENCRYPT_MODE, KEY);
            y = Cipher.getInstance(ALGORITHM);
            y.init(Cipher.DECRYPT_MODE, KEY);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get algorithm", e);
        }

        ENCRYPTION_CIPHER = x == null ? null : x;
        DECRYPTION_CIPHER = y == null ? null : y;
    }

    @Override
    public String convertToDatabaseColumn(String toEncrypt) {
        if (toEncrypt == null) {
            return null;
        }

        try {
            return Base64.getEncoder()
                    .encodeToString(ENCRYPTION_CIPHER.doFinal(toEncrypt.getBytes(UTF_8)));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to encrypt database column", e);
            return null;
        }
    }

    @Override
    public String convertToEntityAttribute(String toDecrypt) {
        if (toDecrypt == null) {
            return null;
        }

        try {
            return new String(
                    DECRYPTION_CIPHER.doFinal(Base64.getDecoder().decode(toDecrypt)),
                    UTF_8
            );
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to decrypt database column", e);
            return null;
        }
    }
}

