package com.oauth2cloud.server.hibernate.converter;

import org.testng.annotations.Test;

import static com.oauth2cloud.server.hibernate.converter.EncryptedStringConverter.*;
import static org.testng.Assert.assertEquals;

public class EncryptedStringConverterTest {
    @Test
    public void testEncryption() throws Exception {
        init("xTUf4mP2SI6nfeLO");
        assertEquals(decrypt(encrypt("hello")), "hello");
    }
}