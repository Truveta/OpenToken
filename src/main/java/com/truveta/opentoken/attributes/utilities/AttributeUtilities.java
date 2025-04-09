/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.attributes.utilities;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * This class includes functions such as normalizing accents,  
 * standardizing formats, and other attribute-related transformations.  
 * 
 */
public class AttributeUtilities {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
    
    private AttributeUtilities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String removeDiacritics(String value) {
        return DIACRITICS.matcher(Normalizer.normalize(value.trim(), Normalizer.Form.NFD)).replaceAll("");
    }
}



