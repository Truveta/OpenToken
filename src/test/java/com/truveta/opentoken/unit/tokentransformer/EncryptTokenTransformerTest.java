/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.unit.tokentransformer;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.truveta.opentoken.tokentransformer.EncryptTokenTransformer;

class EncryptTokenTransformerTest {
    private EncryptTokenTransformer transformer;
    private static final String VALID_KEY = "12345678901234567890123456789012"; // 32-byte key
    private static final String INVALID_KEY = "short-key"; // Invalid short key

    @BeforeEach
    void setUp() throws Exception {
        transformer = new EncryptTokenTransformer(VALID_KEY);
    }

    @Test
    void testConstructor_ValidKey_Success() throws Exception {
        EncryptTokenTransformer validTransformer = new EncryptTokenTransformer(VALID_KEY);
        Assertions.assertNotNull(validTransformer);
    }

    @Test
    void testConstructor_InvalidKeyLength_ThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(InvalidKeyException.class, () -> {
            new EncryptTokenTransformer(INVALID_KEY); // Key is too short
        });
        Assertions.assertEquals("Key must be 32 characters long", exception.getMessage());
    }

    @Test
    void testTransform_ValidToken_ReturnsEncryptedToken() throws Exception {
        String token = "mySecretToken";
        String encryptedToken = transformer.transform(token);

        // Ensure the encrypted token is not null or empty
        Assertions.assertNotNull(encryptedToken);
        Assertions.assertFalse(encryptedToken.isEmpty());

        // check if the token is base64-encoded by decoding it
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedToken);
        Assertions.assertNotNull(decodedBytes);
    }

    @Test
    void testTransform_ReversibleEncryption() throws Exception {
        // Testing if encryption followed by decryption will give back the original
        // token.
        String token = "mySecretToken";

        String encryptedToken = transformer.transform(token); // Encrypt the token

        byte[] messageBytes = Base64.getDecoder().decode(encryptedToken);
        byte[] iv = new byte[32];
        byte[] cipherBytes = new byte[messageBytes.length - 32];

        System.arraycopy(messageBytes, 0, iv, 0, 32);
        System.arraycopy(messageBytes, 32, cipherBytes, 0, cipherBytes.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); // Decrypt the token using the same settings
        SecretKeySpec secretKey = new SecretKeySpec(VALID_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        String decryptedToken = new String(decryptedBytes, StandardCharsets.UTF_8);

        Assertions.assertEquals(token, decryptedToken); // Ensure the decrypted token matches the original token
    }
}
