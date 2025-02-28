/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.io.csv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.truveta.opentoken.io.PersonAttributesWriter;

/**
 * The PersonAttributeCSVWriter class is responsible for writing person
 * attributes to a CSV file.
 * It implements the {@link PersonAttributeWriter} interface.
 */
public class PersonAttributesCSVWriter implements PersonAttributesWriter {
    private static final Logger logger = LoggerFactory.getLogger(PersonAttributesCSVWriter.class.getName());

    private final BufferedWriter fileWriter;
    private final CSVPrinter csvPrinter;
    private boolean headerWritten = false;

    /**
     * Initialize the class with the output file in CSV format.
     * 
     * @param filePath the output file path
     * @throws IOException if an I/O error occurs
     */
    public PersonAttributesCSVWriter(String filePath) throws IOException {
        fileWriter = new BufferedWriter(new FileWriter(filePath));
        csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
    }

    @Override
    public void writeAttributes(Map<String, String> data) {

        try {
            if (!headerWritten) {
                // Write the header
                csvPrinter.printRecord(data.keySet());
                headerWritten = true;
            }

            csvPrinter.printRecord(data.values());

        } catch (IOException e) {
            logger.error("Error in writing CSV file: {}", e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        this.csvPrinter.close();
        this.fileWriter.close();
    }

}
