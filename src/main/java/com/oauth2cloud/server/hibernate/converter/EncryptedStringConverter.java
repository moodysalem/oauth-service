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
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding", UTF_8 = "UTF-8";
    private static Cipher ENCRYPTION_CIPHER, DECRYPTION_CIPHER;

    public synchronized static void init(final String secret) {
        final Key secretKey = new SecretKeySpec(secret.getBytes(), "AES");
        try {
            ENCRYPTION_CIPHER = Cipher.getInstance(ALGORITHM);
            ENCRYPTION_CIPHER.init(Cipher.ENCRYPT_MODE, secretKey);
            DECRYPTION_CIPHER = Cipher.getInstance(ALGORITHM);
            DECRYPTION_CIPHER.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(final String toEncrypt) {
        if (toEncrypt == null) {
            return null;
        }

        try {
            return Base64.getEncoder()
                    .encodeToString(
                            ENCRYPTION_CIPHER.doFinal(
                                    toEncrypt.getBytes(UTF_8)
                            )
                    );
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to encrypt database column", e);
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(final String toDecrypt) {
        if (toDecrypt == null) {
            return null;
        }

        try {
            return new String(
                    DECRYPTION_CIPHER.doFinal(
                            Base64.getDecoder()
                                    .decode(toDecrypt)
                    ),
                    UTF_8
            );
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to decrypt database column", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String convertToDatabaseColumn(final String toEncrypt) {
        return encrypt(toEncrypt);
    }

    @Override
    public String convertToEntityAttribute(final String toDecrypt) {
        return decrypt(toDecrypt);
    }
}

