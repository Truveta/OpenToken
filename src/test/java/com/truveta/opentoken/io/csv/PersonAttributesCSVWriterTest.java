/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.io.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PersonAttributesCSVWriterTest {

    private File tempFile;
    private PersonAttributesCSVWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("test_person_attributes_", ".csv").toFile();
        writer = new PersonAttributesCSVWriter(tempFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (writer != null) {
            writer.close();
        }
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testWriteSingleRecord() throws Exception {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("RecordId", "123");
        data.put("Name", "John Doe");
        data.put("SocialSecurityNumber", "123-45-6789");

        writer.writeAttributes(data);
        writer.close();

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String header = reader.readLine();
            assertEquals("RecordId,Name,SocialSecurityNumber", header);

            String record = reader.readLine();
            assertEquals("123,John Doe,123-45-6789", record);
        }
    }

    @Test
    void testWriteMultipleRecords() throws Exception {
        Map<String, String> data1 = new LinkedHashMap<>();
        data1.put("RecordId", "123");
        data1.put("Name", "John Doe");
        data1.put("SocialSecurityNumber", "123-45-6789");

        Map<String, String> data2 = new LinkedHashMap<>();
        data2.put("RecordId", "456");
        data2.put("Name", "Jane Smith");
        data2.put("SocialSecurityNumber", "987-65-4321");

        writer.writeAttributes(data1);
        writer.writeAttributes(data2);
        writer.close();

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String header = reader.readLine();
            assertEquals("RecordId,Name,SocialSecurityNumber", header);

            String record1 = reader.readLine();
            assertEquals("123,John Doe,123-45-6789", record1);

            String record2 = reader.readLine();
            assertEquals("456,Jane Smith,987-65-4321", record2);
        }
    }

    @Test
    void testHeaderWrittenOnlyOnce() throws Exception {
        Map<String, String> data1 = new LinkedHashMap<>();
        data1.put("RecordId", "123");
        data1.put("Name", "John Doe");

        Map<String, String> data2 = new LinkedHashMap<>();
        data2.put("RecordId", "456");
        data2.put("Name", "Jane Smith");

        writer.writeAttributes(data1);
        writer.writeAttributes(data2);
        writer.close();

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String header = reader.readLine();
            assertEquals("RecordId,Name", header);

            String record1 = reader.readLine();
            assertEquals("123,John Doe", record1);

            String record2 = reader.readLine();
            assertEquals("456,Jane Smith", record2);
        }
    }
}