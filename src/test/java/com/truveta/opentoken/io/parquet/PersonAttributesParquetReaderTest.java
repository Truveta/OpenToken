/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.io.parquet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties.WriterVersion;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.schema.MessageTypeParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.truveta.opentoken.attributes.Attribute;
import com.truveta.opentoken.attributes.general.RecordIdAttribute;
import com.truveta.opentoken.attributes.person.FirstNameAttribute;
import com.truveta.opentoken.attributes.person.SocialSecurityNumberAttribute;

class PersonAttributesParquetReaderTest {

    private File tempFile;
    private String tempFilePath;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("test_data", ".parquet").toFile();
        tempFilePath = tempFile.getAbsolutePath();
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testReadParquet() throws Exception {
        try (PersonAttributesParquetWriter writer = new PersonAttributesParquetWriter(tempFilePath)) {

            Map<String, String> record1 = new HashMap<>();
            record1.put("RecordId", "1");
            record1.put("SocialSecurityNumber", "123-45-6789");
            record1.put("FirstName", "John");
            writer.writeAttributes(record1);

            Map<String, String> record2 = new HashMap<>();
            record2.put("RecordId", "2");
            record2.put("SocialSecurityNumber", "987-65-4321");
            record2.put("FirstName", "Jane");
            writer.writeAttributes(record2);
        }

        try (PersonAttributesParquetReader reader = new PersonAttributesParquetReader(tempFilePath)) {
            assertTrue(reader.hasNext());

            Map<Class<? extends Attribute>, String> firstRecord = reader.next();
            assertEquals("1", firstRecord.get(RecordIdAttribute.class));
            assertEquals("123-45-6789", firstRecord.get(SocialSecurityNumberAttribute.class));
            assertEquals("John", firstRecord.get(FirstNameAttribute.class));

            assertTrue(reader.hasNext());

            Map<Class<? extends Attribute>, String> secondRecord = reader.next();
            assertEquals("2", secondRecord.get(RecordIdAttribute.class));
            assertEquals("987-65-4321", secondRecord.get(SocialSecurityNumberAttribute.class));
            assertEquals("Jane", secondRecord.get(FirstNameAttribute.class));

            assertFalse(reader.hasNext());
        }
    }

    @Test
    void testReadEmptyParquet() throws Exception {
        var conf = new Configuration();
        String schemaString = "message PersonAttributes {\n" +
                "  required binary BirthDate (UTF8);\n" +
                "  required binary Gender (UTF8);\n" +
                "  required binary FirstName (UTF8);\n" +
                "  required binary SocialSecurityNumber (UTF8);\n" +
                "  required binary RecordId (UTF8);\n" +
                "  required binary PostalCode (UTF8);\n" +
                "  required binary LastName (UTF8);\n" +
                "}";
        var schema = MessageTypeParser.parseMessageType(schemaString);
        GroupWriteSupport.setSchema(schema, conf);
        Path path = new Path(tempFilePath);

        var writer = ExampleParquetWriter.builder(path)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .withWriterVersion(WriterVersion.PARQUET_2_0)
                .withConf(conf)
                .withType(schema)
                .build();
        writer.close();

        try (PersonAttributesParquetReader reader = new PersonAttributesParquetReader(tempFilePath)) {
            assertFalse(reader.hasNext());
        }
    }

    @Test
    void testHasNext() throws Exception {
        try (PersonAttributesParquetWriter writer = new PersonAttributesParquetWriter(tempFilePath)) {

            Map<String, String> record1 = new HashMap<>();
            record1.put("RecordId", "1");
            record1.put("SocialSecurityNumber", "123-45-6789");
            record1.put("FirstName", "John");
            writer.writeAttributes(record1);
        }

        try (PersonAttributesParquetReader reader = new PersonAttributesParquetReader(tempFilePath)) {
            assertTrue(reader.hasNext());
            reader.next();
            assertFalse(reader.hasNext());
        }
    }

    @Test
    void testNext() throws Exception {
        try (PersonAttributesParquetWriter writer = new PersonAttributesParquetWriter(tempFilePath)) {

            Map<String, String> record1 = new HashMap<>();
            record1.put("RecordId", "1");
            record1.put("SocialSecurityNumber", "123-45-6789");
            record1.put("FirstName", "John");
            writer.writeAttributes(record1);
        }

        try (PersonAttributesParquetReader reader = new PersonAttributesParquetReader(tempFilePath)) {
            boolean hasNext = reader.hasNext();
            assertTrue(hasNext);

            Map<Class<? extends Attribute>, String> record = reader.next();
            assertNotNull(record);
            assertEquals("1", record.get(RecordIdAttribute.class));
            assertEquals("123-45-6789", record.get(SocialSecurityNumberAttribute.class));
            assertEquals("John", record.get(FirstNameAttribute.class));
        }
    }

    @Test
    void testClose() throws Exception {
        try (PersonAttributesParquetWriter writer = new PersonAttributesParquetWriter(tempFilePath)) {

            Map<String, String> record1 = new HashMap<>();
            record1.put("RecordId", "1");
            record1.put("SocialSecurityNumber", "123-45-6789");
            record1.put("FirstName", "John Doe");
            writer.writeAttributes(record1);
        }

        PersonAttributesParquetReader reader = new PersonAttributesParquetReader(tempFilePath);
        reader.close();
        assertThrows(NoSuchElementException.class, reader::hasNext);
        assertThrows(NoSuchElementException.class, reader::next);
    }

    @Test
    void testConstructorThrowsIOException() {
        String invalidtempFilePath = "non_existent_file.parquet";
        assertThrows(IOException.class, () -> new PersonAttributesParquetReader(invalidtempFilePath));
    }
}