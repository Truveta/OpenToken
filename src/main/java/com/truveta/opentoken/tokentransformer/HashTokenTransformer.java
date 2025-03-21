/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.tokentransformer;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the token using a cryptographic hash function with
 * a secret key.
 * 
 * @see <a href=https://datatracker.ietf.org/doc/html/rfc4868>HMACSHA256</a>
 */
public class HashTokenTransformer implements TokenTransformer {
    private static final Logger logger = LoggerFactory.getLogger(HashTokenTransformer.class.getName());

    public static final int NUMBEROFBYTES = 32;

    private final Mac mac;
    private final Encoder encoder;

    /**
     * Initializes the underlying MAC with the secret key.
     * 
     * @param hashingSecret the cryptographic secret key.
     * 
     * @throws java.security.NoSuchAlgorithmException invalid HMAC algorithm.
     * @throws java.security.InvalidKeyException      if the given key is
     *                                                inappropriate for
     *                                                initializing this HMAC.
     */
    public HashTokenTransformer(String hashingSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        if (hashingSecret == null || hashingSecret.isBlank()) {
            this.mac = null;
            this.encoder = null;
            return;
        }
        this.mac = Mac.getInstance("HmacSHA256");
        this.mac.init(new SecretKeySpec(hashingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        this.encoder = Base64.getEncoder();
    }

    /**
     * Hash token transformer.
     * <p>
     * The token is transformed using HMAC SHA256 algorithm.
     *
     * @return hashed token in <code>base64</code> format.
     * 
     * @throws java.lang.IllegalArgumentException <code>null</code> or blank token
     *                                            provided.
     * @throws java.lang.IllegalStateException    if the HMAC is not initialized
     *                                            properly.
     */
    @Override
    public String transform(String token) throws IllegalArgumentException, IllegalStateException {
        if (token == null || token.isBlank()) {
            logger.error("Invalid Argument. Token can't be Null.");
            throw new IllegalArgumentException("Invalid Argument. Token can't be Null.");
        }

        synchronized (this.mac) {
            byte[] dataAsBytes = token.getBytes();
            byte[] sha = this.mac.doFinal(dataAsBytes);
            return this.encoder.encodeToString(sha);
        }
    }
}
