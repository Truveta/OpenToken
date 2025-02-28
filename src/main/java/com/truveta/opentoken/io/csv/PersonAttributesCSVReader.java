/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.io.csv;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.truveta.opentoken.io.PersonAttributesReader;
import com.truveta.opentoken.attributes.Attribute;
import com.truveta.opentoken.attributes.AttributeLoader;

/**
 * Reads person attributes from a CSV file.
 * Implements the {@link PersonAttributesReader} interface.
 */
public class PersonAttributesCSVReader implements PersonAttributesReader {
    private static final Logger logger = LoggerFactory.getLogger(PersonAttributesCSVReader.class.getName());

    private final Reader reader;
    private final CSVParser csvParser;
    private Iterator<CSVRecord> iterator;
    private Map<String, Attribute> attributeMap = new HashMap<>();

    /**
     * Initialize the class with the input file in CSV format.
     * 
     * @param filePath the input file path
     * @throws IOException if an I/O error occurs
     */
    public PersonAttributesCSVReader(String filePath) throws IOException {
        try {

            reader = Files.newBufferedReader(Paths.get(filePath));
            csvParser = new CSVParser(reader, CSVFormat.Builder.create().setHeader().build());
            iterator = csvParser.iterator();

            Set<Attribute> attributes = AttributeLoader.load();
            for (String headerName : csvParser.getHeaderNames()) {
                for (Attribute attribute : attributes) {
                    for (String alias : attribute.getAliases()) {
                        if (headerName.equalsIgnoreCase(alias)) {
                            attributeMap.put(headerName, attribute);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error in reading CSV file: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public void close() throws Exception {
        csvParser.close();
        reader.close();
    }

    @Override
    public Map<Class<? extends Attribute>, String> next() {
        CSVRecord record = iterator.next();

        Map<Class<? extends Attribute>, String> personAttributes = new HashMap<>();
        record.toMap().forEach((key, value) -> {
            Attribute attribute = attributeMap.get(key);
            if (attribute != null) {
                personAttributes.put(attribute.getClass(), value);
            }
            // else ignore attribute as it's not supported
        });

        return personAttributes;
    }
}