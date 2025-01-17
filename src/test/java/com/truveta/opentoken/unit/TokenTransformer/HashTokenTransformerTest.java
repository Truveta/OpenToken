/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.unit.tokentransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import javax.crypto.Mac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Base64;

import com.truveta.opentoken.tokentransformer.HashTokenTransformer;

public class HashTokenTransformerTest {
    private static final String VALID_SECRET = "sampleSecret";
    private static final String VALID_TOKEN = "sampleToken";

    private HashTokenTransformer transformer;

    @BeforeEach
    public void setup() throws NoSuchAlgorithmException, InvalidKeyException {
        transformer = new HashTokenTransformer(VALID_SECRET);
    }

    @Test
    public void testTransform_ValidToken_ReturnsHashedToken() throws Exception {
        String hashedToken = transformer.transform(VALID_TOKEN);
        assertNotNull(hashedToken);

        // Manually calculate the expected hash for validation
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(VALID_SECRET.getBytes(), "HmacSHA256"));
        byte[] expectedHash = mac.doFinal(VALID_TOKEN.getBytes());
        String expectedHashedToken = Base64.getEncoder().encodeToString(expectedHash);

        assertEquals(expectedHashedToken, hashedToken);
    }

    @Test
    public void testTransform_NullToken_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transformer.transform(null);
        });
        assertEquals("Invalid Argument. Token can't be Null.", exception.getMessage());
    }

    @Test
    public void testConstructor_NullSecret_InitializesWithNullMac() throws Exception {
        HashTokenTransformer nullSecretTransformer = new HashTokenTransformer(null);
        assertThrows(NullPointerException.class, () -> {
            nullSecretTransformer.transform(VALID_TOKEN);
        });
    }

    @Test
    public void testConstructor_BlankSecret_InitializesWithNullMac() throws Exception {
        HashTokenTransformer blankSecretTransformer = new HashTokenTransformer("");
        assertThrows(NullPointerException.class, () -> {
            blankSecretTransformer.transform(VALID_TOKEN);
        });
    }

    @Test
    public void testTransform_ValidTokenMultipleTimes_ReturnsConsistentHash() throws Exception {
        String hash1 = transformer.transform(VALID_TOKEN);
        String hash2 = transformer.transform(VALID_TOKEN);
        assertEquals(hash1, hash2); // The hashed value should be consistent
    }
}
