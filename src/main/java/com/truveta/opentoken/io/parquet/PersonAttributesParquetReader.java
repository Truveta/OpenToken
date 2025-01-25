/**
 * Copyright (c) Truveta. All rights reserved.
 * 
 * Represents a parquet reader for reading person attributes.
 */
package com.truveta.opentoken.io.parquet;

import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.Type;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.hadoop.conf.Configuration;

import com.truveta.opentoken.io.PersonAttributesReader;

import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.Closeable;

public class PersonAttributesParquetReader implements PersonAttributesReader, Closeable {
    private ParquetReader<Group> reader;
    private Group currentGroup;
    private Iterator<Group> iterator;

    public PersonAttributesParquetReader(String filePath) throws IOException {
        Configuration conf = new Configuration();
        Path path = new Path(filePath);
        GroupReadSupport readSupport = new GroupReadSupport();

        this.reader = ParquetReader.builder(readSupport, path).withConf(conf).build();

        this.iterator = new Iterator<Group>() {
            @Override
            public boolean hasNext() {
                try {
                    currentGroup = reader.read();
                    return currentGroup != null;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public Group next() {
                return currentGroup;
            }
        };
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Map<String, String> next() {
        Group group = iterator.next();
        Map<String, String> attributes = new HashMap<>();
        GroupType schema = group.getType();
        for (Type field : schema.getFields()) {
            String fieldName = field.getName();
            int fieldIndex = schema.getFieldIndex(fieldName);
            if (group.getFieldRepetitionCount(fieldIndex) > 0) {
                String fieldValue = group.getValueToString(fieldIndex, 0);
                attributes.put(fieldName, fieldValue);
            }
        }
        return attributes;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
