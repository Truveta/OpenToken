/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.attributes.person;

import java.util.List;
import com.truveta.opentoken.attributes.BaseAttribute;
import com.truveta.opentoken.attributes.utilities.AttributeUtilities;

/**
 * Represents the last name of a person.
 * 
 * This class extends BaseAttribute and provides functionality for working with
 * last name fields. It recognizes "LastName" and "Surname" as valid aliases for
 * this attribute type.
 * 
 * The attribute performs no normalization on input values, returning them
 * unchanged.
 */
public class LastNameAttribute extends BaseAttribute {

    private static final String NAME = "LastName";
    private static final String[] ALIASES = new String[] { NAME, "Surname" };

    public LastNameAttribute() {
        super(List.of());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public String normalize(String value) {
        return AttributeUtilities.removeDiacritics(value);
    }
}
