/**
 * Copyright (c) Truveta. All rights reserved.
 */
package com.truveta.opentoken.integration.processor;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.truveta.opentoken.io.PersonAttributesCSVReader;
import com.truveta.opentoken.io.PersonAttributesCSVWriter;
import com.truveta.opentoken.io.PersonAttributesReader;
import com.truveta.opentoken.io.PersonAttributesWriter;
import com.truveta.opentoken.processor.PersonAttributesProcessor;
import com.truveta.opentoken.tokentransformer.EncryptTokenTransformer;
import com.truveta.opentoken.tokentransformer.HashTokenTransformer;
import com.truveta.opentoken.tokentransformer.NoOperationTokenTransformer;
import com.truveta.opentoken.tokentransformer.TokenTransformer;

class PersonAttributesProcessorTest {
    final int totalRecordsMatched = 1001;
    final String hash_key = "hash_key";
    final String encryption_key = "the_encryption_key_goes_here....";
    final String hashAlgorithm = "HmacSHA256";
    final String encryptionAlgorithm = "AES";

    /*
     * This test case takes input csv which has repeat probability of 0.30.
     * RecordIds will still be unique.
     * The goal is to ensure that the records with repeated data still generate the
     * same tokens.
     */
    @Test
    void testInputWithDuplicates() throws Exception {
        String inputCsvFile = "src/test/resources/mockdata/test_data.csv";
        Map<String, List<String>> ssnToRecordIdsMap = groupRecordsIdsWithSameSsn(inputCsvFile);

        List<TokenTransformer> tokenTransformerList = new ArrayList<>();
        tokenTransformerList.add(new HashTokenTransformer(hash_key));
        tokenTransformerList.add(new EncryptTokenTransformer(encryption_key));
        ArrayList<Map<String, String>> resultFromPersonAttributesProcessor = readCSV_fromPersonAttributesProcessor(
                inputCsvFile, tokenTransformerList);

        for (String processedCsvMapKey : ssnToRecordIdsMap.keySet()) {
            List<String> recordIds = ssnToRecordIdsMap.get(processedCsvMapKey);

            int count = 0;
            List<String> tokenGenerated = new ArrayList<>();

            for (Map<String, String> recordToken : resultFromPersonAttributesProcessor) {
                String recordId = recordToken.get("RecordId");
                /*
                 * This code block checks that for multiple recordIds with same SSN
                 * the 5 tokens generated (for each recordId) are always the same
                 */
                if (recordIds.contains(recordId)) {
                    String token = recordToken.get("Token");
                    // for a new RecordId simply store the 5 tokens as a list
                    if (tokenGenerated.size() < 5) {
                        tokenGenerated.add(recordToken.get("Token"));
                    }
                    // for RecordId with same SSN, tokens should match as in the list
                    else if (tokenGenerated.size() == 5) { // assertion to check existing tokens match for duplicate
                                                           // records
                        Assertions.assertTrue(tokenGenerated.contains(token));
                    }
                    count++;
                }
            }
            Assertions.assertEquals(count, recordIds.size() * 5);
        }
    }

    /*
     * This test case comapres two input csv's. A section of these data will
     * overlapp with both the csv's.
     * The first csv is hashed and encrypted and the second csv is only hashed.
     * The goal is to ensure that tokenization process still generates the tokens
     * correctly for both the csv's.
     * The test case then ensures the tokens match for overlapping records.
     * This is done by decrypting the encrypted tokens for the first csv and hashing
     * the tokens in second csv.
     * Finally we find exact matches in both files.
     */
    @Test
    void testInputWithOverlappingData() throws Exception {
        // Incoming file is hashed and encrypted
        List<TokenTransformer> tokenTransformerList = new ArrayList<>();
        tokenTransformerList.add(new HashTokenTransformer(hash_key));
        tokenTransformerList.add(new EncryptTokenTransformer(encryption_key));
        ArrayList<Map<String, String>> resultFromPersonAttributesProcessor1 = readCSV_fromPersonAttributesProcessor(
                "src/test/resources/mockdata/test_overlap1.csv", tokenTransformerList);

        // Truveta file is neither hashed nor encrypted
        tokenTransformerList = new ArrayList<>();
        tokenTransformerList.add(new NoOperationTokenTransformer());
        ArrayList<Map<String, String>> resultFromPersonAttributesProcessor2 = readCSV_fromPersonAttributesProcessor(
                "src/test/resources/mockdata/test_overlap2.csv", tokenTransformerList);

        Map<String, String> recordIdToTokenMap1 = new HashMap<>();
        // tokens from incoming file are hashed and encrypted. This needs decryption
        for (Map<String, String> recordToken1 : resultFromPersonAttributesProcessor1) {
            String encryptedToken = recordToken1.get("Token");
            recordIdToTokenMap1.put(recordToken1.get("RecordId"), decryptToken(encryptedToken));
        }

        Map<String, String> recordIdToTokenMap2 = new HashMap<>();
        // Truveta tokens are neither hashed nor encrypted. This needs to be hashed
        for (Map<String, String> recordToken2 : resultFromPersonAttributesProcessor2) {
            String noOpToken = recordToken2.get("Token");
            // hashing this token to match incoming records files
            recordIdToTokenMap2.put(recordToken2.get("RecordId"), hashToken(noOpToken));
        }

        // Now both are similarly hased (Hmac hash)
        int overlappCount = 0;
        for (String recordId1 : recordIdToTokenMap1.keySet()) {
            String token1 = recordIdToTokenMap1.get(recordId1);
            if (recordIdToTokenMap2.containsKey(recordId1)) {
                overlappCount++;
                Assertions.assertTrue(recordIdToTokenMap2.get(recordId1).equals(token1),
                        "For same RecordIds the tokens must match");
            }
        }
        Assertions.assertEquals(overlappCount, totalRecordsMatched);
    }

    @Test
    void testInputBackwardCompatibility() throws Exception {
        String oldTmpInputFile = Files.createTempFile("person_attributes_old", ".csv").toString();
        String newTmpInputFile = Files.createTempFile("person_attributes_new", ".csv").toString();

        String oldTmpOutputFile = Files.createTempFile("person_attributes_old_out", ".csv").toString();
        String newTmpOutputFile = Files.createTempFile("person_attributes_new_out", ".csv").toString();

        // Person attributes to be used for token generation
        Map<String, String> personAttributes = new HashMap<>();
        personAttributes.put("FirstName", "Alice");
        personAttributes.put("LastName", "Wonderland");
        personAttributes.put("SocialSecurityNumber", "345-54-6795");
        personAttributes.put("PostalCode", "98052");
        personAttributes.put("BirthDate", "1993-08-10");
        personAttributes.put("Sex", "Female");

        try (PersonAttributesWriter writer = new PersonAttributesCSVWriter(newTmpInputFile)) {
            writer.writeAttributes(personAttributes);
        }

        personAttributes.remove("Sex");
        personAttributes.put("Gender", "Female");

        try (PersonAttributesWriter writer = new PersonAttributesCSVWriter(oldTmpInputFile)) {
            writer.writeAttributes(personAttributes);
        }

        // Truveta file is neither hashed nor encrypted
        List<TokenTransformer> tokenTransformers = new ArrayList<>();
        tokenTransformers.add(new NoOperationTokenTransformer());

        try (PersonAttributesReader reader = new PersonAttributesCSVReader(
                newTmpInputFile);
                PersonAttributesWriter writer = new PersonAttributesCSVWriter(newTmpOutputFile)) {

            PersonAttributesProcessor.process(reader, writer, tokenTransformers);
        }

        try (PersonAttributesReader reader = new PersonAttributesCSVReader(
                oldTmpInputFile);
                PersonAttributesWriter writer = new PersonAttributesCSVWriter(oldTmpOutputFile)) {

            PersonAttributesProcessor.process(reader, writer, tokenTransformers);
        }

        // read oldTmpOutputFile and newTmpOutputFile as strings and assert equality
        String oldOutput = Files.readString(FileSystems.getDefault().getPath(oldTmpOutputFile));
        String newOutput = Files.readString(FileSystems.getDefault().getPath(newTmpOutputFile));
        Assertions.assertEquals(oldOutput, newOutput);
    }

    ArrayList<Map<String, String>> readCSV_fromPersonAttributesProcessor(String inputCsvFilePath,
            List<TokenTransformer> tokenTransformers) throws Exception {

        String tmpOutputFile = Files.createTempFile("person_attributes_", ".csv").toString();
        try (PersonAttributesReader reader = new PersonAttributesCSVReader(inputCsvFilePath);
                PersonAttributesWriter writer = new PersonAttributesCSVWriter(tmpOutputFile)) {

            PersonAttributesProcessor.process(reader, writer, tokenTransformers);
        }

        ArrayList<Map<String, String>> result = new ArrayList<>();

        try (PersonAttributesReader reader = new PersonAttributesCSVReader(tmpOutputFile)) {
            while (reader.hasNext()) {
                result.add(reader.next());
            }
        }
        return result;
    }

    /*
     * Returns Map of SSN -> List of RecordIds
     */
    Map<String, List<String>> groupRecordsIdsWithSameSsn(String inputCsvFilePath) throws Exception {
        Map<String, List<String>> ssnToRecordIdsMap = new HashMap<>();

        try (PersonAttributesReader reader = new PersonAttributesCSVReader(inputCsvFilePath)) {

            while (reader.hasNext()) {
                Map<String, String> row = reader.next();

                String ssn = row.get("SocialSecurityNumber");
                List<String> recordIds = ssnToRecordIdsMap.getOrDefault(ssn, new ArrayList<>());
                recordIds.add(row.get("RecordId"));
                ssnToRecordIdsMap.put(ssn, recordIds);
            }

        }

        return ssnToRecordIdsMap;
    }

    private String hashToken(String noOpToken) throws Exception {
        Mac mac = Mac.getInstance(hashAlgorithm);
        mac.init(new SecretKeySpec(hash_key.getBytes(StandardCharsets.UTF_8), hashAlgorithm));
        byte[] dataAsBytes = noOpToken.getBytes();
        byte[] sha = mac.doFinal(dataAsBytes);
        return Base64.getEncoder().encodeToString(sha);
    }

    private String decryptToken(String encryptedToken) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); // Decrypt the token using the same encryption
                                                                    // settings
        SecretKeySpec secretKey = new SecretKeySpec(encryption_key.getBytes(), encryptionAlgorithm);
        IvParameterSpec iv = new IvParameterSpec(new byte[16]); // 16-byte IV (all zeroes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
