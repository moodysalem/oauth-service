package com.oauth2cloud.server.hibernate.validate;

import org.testng.annotations.Test;

import static com.oauth2cloud.server.hibernate.validate.NoSpacesValidator.noSpaces;
import static org.testng.Assert.*;

public class NoSpacesValidatorTest {
    @Test
    public void testNoSpaces() throws Exception {
        assertTrue(noSpaces("hello_world"));
        assertTrue(noSpaces(""));
        assertTrue(noSpaces(null));

        assertTrue(!noSpaces("hello world"));
        assertTrue(!noSpaces(" "));
        assertTrue(!noSpaces(" hello_world "));
    }
}