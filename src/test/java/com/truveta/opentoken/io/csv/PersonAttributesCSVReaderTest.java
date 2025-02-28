/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.io.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.truveta.opentoken.attributes.Attribute;
import com.truveta.opentoken.attributes.general.RecordIdAttribute;
import com.truveta.opentoken.attributes.person.FirstNameAttribute;
import com.truveta.opentoken.attributes.person.LastNameAttribute;
import com.truveta.opentoken.attributes.person.SocialSecurityNumberAttribute;

class PersonAttributesCSVReaderTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test_data", ".csv");
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testReadValidCSV() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("RecordId,SocialSecurityNumber,FirstName,LastName\n");
            writer.write("1,123-45-6789,John,Doe\n");
            writer.write("2,987-65-4321,Jane,Smith\n");
        }

        try (PersonAttributesCSVReader reader = new PersonAttributesCSVReader(tempFile.getAbsolutePath())) {
            assertTrue(reader.hasNext());

            Map<Class<? extends Attribute>, String> firstRecord = reader.next();
            assertEquals("1", firstRecord.get(RecordIdAttribute.class));
            assertEquals("123-45-6789", firstRecord.get(SocialSecurityNumberAttribute.class));
            assertEquals("John", firstRecord.get(FirstNameAttribute.class));
            assertEquals("Doe", firstRecord.get(LastNameAttribute.class));

            assertTrue(reader.hasNext());

            Map<Class<? extends Attribute>, String> secondRecord = reader.next();
            assertEquals("2", secondRecord.get(RecordIdAttribute.class));
            assertEquals("987-65-4321", secondRecord.get(SocialSecurityNumberAttribute.class));
            assertEquals("Jane", secondRecord.get(FirstNameAttribute.class));
            assertEquals("Smith", secondRecord.get(LastNameAttribute.class));

            assertFalse(reader.hasNext());
        }
    }

    @Test
    void testReadEmptyCSV() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("RecordId,SocialSecurityNumber,Name\n");
        }

        try (PersonAttributesCSVReader reader = new PersonAttributesCSVReader(tempFile.getAbsolutePath())) {
            assertFalse(reader.hasNext());
        }
    }

    @Test
    void testHasNext() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("RecordId,SocialSecurityNumber,FirstName,LastName\n");
            writer.write("1,123-45-6789,John,Doe\n");
        }

        try (PersonAttributesCSVReader reader = new PersonAttributesCSVReader(tempFile.getAbsolutePath())) {
            assertTrue(reader.hasNext());
            reader.next();
            assertFalse(reader.hasNext());
        }
    }

    @Test
    void testNext() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("RecordId,SocialSecurityNumber,FirstName,LastName\n");
            writer.write("1,123-45-6789,John,Doe\n");
        }

        try (PersonAttributesCSVReader reader = new PersonAttributesCSVReader(tempFile.getAbsolutePath())) {
            Map<Class<? extends Attribute>, String> record = reader.next();
            assertNotNull(record);
            assertEquals("1", record.get(RecordIdAttribute.class));
            assertEquals("123-45-6789", record.get(SocialSecurityNumberAttribute.class));
            assertEquals("John", record.get(FirstNameAttribute.class));
            assertEquals("Doe", record.get(LastNameAttribute.class));
        }
    }

    @Test
    void testClose() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("RecordId,SocialSecurityNumber,Name\n");
            writer.write("1,123-45-6789,John Doe\n");
        }

        PersonAttributesCSVReader reader = new PersonAttributesCSVReader(tempFile.getAbsolutePath());
        reader.close();
        assertThrows(NoSuchElementException.class, reader::next);
    }

    @Test
    void testConstructorThrowsIOException() {
        String invalidFilePath = "non_existent_file.csv";
        assertThrows(IOException.class, () -> new PersonAttributesCSVReader(invalidFilePath));
    }
}