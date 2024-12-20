// Copyright (c) Truveta. All rights reserved.
package com.truveta.opentoken.tokens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 * @see com.truveta.opentoken.tokens.AttributeExpression AttributeExpression
 */
public class TokenDefinition implements BaseTokenDefinition {
    private final Map<String, ArrayList<AttributeExpression>> definitions;

    /**
     * Initializes the token definitions.
     */
    public TokenDefinition() {
        // Token 1
        var t1 = new ArrayList<AttributeExpression>();
        t1.add(new AttributeExpression(LAST_NAME, "T|U"));
        t1.add(new AttributeExpression(FIRST_NAME, "T|S(0,1)|U"));
        t1.add(new AttributeExpression(GENDER, "T|U"));
        t1.add(new AttributeExpression(BIRTH_DATE, "T|D"));

        // Token 2 
        var t2 = new ArrayList<AttributeExpression>();
        t2.add(new AttributeExpression(LAST_NAME, "T|U"));
        t2.add(new AttributeExpression(FIRST_NAME, "T|U"));
        t2.add(new AttributeExpression(BIRTH_DATE, "T|D"));
        t2.add(new AttributeExpression(POSTAL_CODE, "T|S(0,3)|U"));

        // Token 3 
        var t3 = new ArrayList<AttributeExpression>();
        t3.add(new AttributeExpression(LAST_NAME, "T|U"));
        t3.add(new AttributeExpression(FIRST_NAME, "T|U"));
        t3.add(new AttributeExpression(GENDER, "T|U"));
        t3.add(new AttributeExpression(BIRTH_DATE, "T|D"));

        // Token 4
        var t4 = new ArrayList<AttributeExpression>();
        t4.add(new AttributeExpression(SOCIAL_SECURITY_NUMBER, "T|M(\\d+)"));
        t4.add(new AttributeExpression(GENDER, "T|U"));
        t4.add(new AttributeExpression(BIRTH_DATE, "T|D"));

        // Token 5
        var t5 = new ArrayList<AttributeExpression>();
        t5.add(new AttributeExpression(LAST_NAME, "T|U"));
        t5.add(new AttributeExpression(FIRST_NAME, "T|S(0,3)|U"));
        t5.add(new AttributeExpression(GENDER, "T|U"));

        this.definitions = new HashMap<>();        
        this.definitions.put("T1", t1);
        this.definitions.put("T2", t2);
        this.definitions.put("T3", t3);
        this.definitions.put("T4", t4);
        this.definitions.put("T5", t5);
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
    public ArrayList<AttributeExpression> getTokenDefinition(String tokenId) {
        return definitions.get(tokenId);
    }
}