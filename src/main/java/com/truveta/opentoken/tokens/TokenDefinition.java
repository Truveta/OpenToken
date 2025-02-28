/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.tokens;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.truveta.opentoken.attributes.AttributeExpression;

/**
 * Encapsulates the token definitions.
 * 
 * <p>
 * The tokens are generated using some token generation rules. This class
 * encapsulates the definition of those rules. Together, they are commonly
 * referred to as <b>token definitions</b> or <b>rule definitions</b>.
 * 
 * <p>
 * Each token/rule definition is a collection of
 * <code>AttributeExpression</code> that are concatenated together to get
 * the token signature.
 * 
 * @see com.truveta.opentoken.attributes.AttributeExpression
 *      AttributeExpression
 */
public class TokenDefinition implements BaseTokenDefinition {
    private final Map<String, List<AttributeExpression>> definitions;

    /**
     * Initializes the token definitions.
     */
    public TokenDefinition() {
        // load all implementations of Token interface and store in definitions
        definitions = new HashMap<>();

        Reflections reflections = new Reflections(TokenDefinition.class.getPackageName());
        Set<Class<? extends Token>> tokenClasses = reflections.getSubTypesOf(Token.class);

        for (Class<? extends Token> tokenClass : tokenClasses) {
            try {
                Token token = tokenClass.getDeclaredConstructor().newInstance();
                definitions.put(token.getIdentifier(), token.getDefinition());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public Set<String> getTokenIdentifiers() {
        return definitions.keySet();
    }

    @Override
    public List<AttributeExpression> getTokenDefinition(String tokenId) {
        return definitions.get(tokenId);
    }
}