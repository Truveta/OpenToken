# Open Token

## Introduction

Truveta’s approach to person matching relies on building a set of matching tokens (or token signatures) per person which are derived from deterministic person data but preserve privacy by using cryptographically secure hashing algorithms.

## Token generation strategy

Tokens are cryptographically secure hashes computed from multiple deterministic person attributes. Tokens are created based on a set of `token generation rules`. Truveta uses multiple distinct token generation rules that define a set of person attributes and which parts of those attributes to use for token generation. Person attributes include:

- `record-id` Unique identification for the record.
- `first-name`
- `last-name`
- `postal-code` Acceptable format: `ddddd` or `ddddd-dddd` where `d` is a numeric digit (0-9).
- `gender` Acceptable format: `Male|Female`.
- `birth-date` Acceptable format: `YYYY-MM-DD` where `MM` is (01-12), `DD` is (01-31).
- `social-security-number` Acceptable format: `ddd-dd-dddd` where `d` is a numeric digit (0-9).

### Sample token generation rules

Rule ID | Rule Definition                                          |
--------|----------------------------------------------------------|
T1      | `U(last-name)\|U(first-name-1)\|U(gender)\|birth-date`   |
T2      | `U(last-name)\|U(first-name)\|birth-date\|postal-code-3` |
T3      | `U(last-name)\|U(first-name)\|U(gender)\|birth-date`     |
T4      | `social-security-number\|U(gender)\|birth-date`          |
T5      | `U(last-name)\|U(first-name-3)\|U(gender)`               |

> U(X) = uppercase(X)<br>
> attribute-N = take first N characters from the `attribute`

### Rules for token generation

A token signature is generated first for every token generation rule. The token signature is then cryptographically hashed and hex encoded to generate the token.

> $Token(R) = Hex(Sha256(TokenSignature(R)))$ where R is the rule ID.<br>
> The token is then transformed further using the formula below:<br>
> $Base64(AESEncrypt(Base64(HMACSHA256(Token(R)))))$<br>

### Example

Given a person with the following attributes:

```csv
RecordId,FirstName,LastName,PostalCode,Gender,BirthDate,SocialSecurityNumber
891dda6c-961f-4154-8541-b48fe18ee620,John,Doe,11111,Male,2000-01-01,000-00-0000
```

The token generation rules above generate the following token signatures:

Rule ID | Token Signature                 | Token                                                              |
--------|---------------------------------|--------------------------------------------------------------------|
T1      | `DOE\|J\|MALE\|2000-01-01`      | `298a70cef9449dbd45b9e9e4cdcc7e3f2ed1e56f677c57702e89ebb133e541b0` |
T2      | `DOE\|JOHN\|2000-01-01\|111`    | `5e5c1f512807d769e8babc8a42891c2550ff57e74ff5f4386ac42869b53bed10` |
T3      | `DOE\|JOHN\|MALE\|2000-01-01`   | `5df7c60d82729359b63b1cdf99aa3c91c462c07c69b17d264e25173e836ce1be` |
T4      | `000-00-0000\|MALE\|2000-01-01` | `02e1798c9feab464d274f2a5856493b96d8e41c2c56f080a8234d403e11dcb49` |
T5      | `DOE\|JOH\|MALE`                | `a3c0feb1e9e83623d339f7147d58bbf6448d8379dc21f531091e561b2d78fb88` |

### Open token data flow

![open-token-data-flow](./open-token-data-flow.jpg)

## Open token overview

This library focuses primarily on token generation. Even though the person matching process is beyond the scope of this library, this document discusses how these tokens work in a person matching system.

As noted above, N distinct tokens are generated for each person using this library. The output of this process is below for three person records r1, r2, and r3:

RecordId | RuleId | Token(RecordId, RuleId)
---------|--------|------------------------
r1       | T1     | Token(r1,T1)
r1       | T2     | Token(r1,T2)
r1       | T3     | Token(r1,T3)
r1       | T4     | Token(r1,T4)
r1       | T5     | Token(r1,T5)
r2       | T1     | Token(r2,T1)
r2       | T2     | Token(r2,T2)
r2       | T3     | Token(r2,T3)
r2       | T4     | Token(r2,T4)
r2       | T5     | Token(r2,T5)
r3       | T1     | Token(r3,T1)
r3       | T2     | Token(r3,T2)
r3       | T3     | Token(r3,T3)
r3       | T4     | Token(r3,T4)
r3       | T5     | Token(r3,T5)

If tokens are generated for persons from multiple data sources, person matching systems can identify a person match if the tokens for a person from one data source matches tokens for another person from a different data source. In the picture below, all tokens for **r3** and **r4** match, and as such r3 and r4 are considered a match.

![open-token-system](./open-token-system.jpg)

## Library driver

A driver is provided so that the library code can be executed easily.

### Execution

#### Via Shell

The driver code could be invoked using:

```shell
java -jar open-token-<version>.jar -i <input-file> -t csv -o <output-file> -h "xb7...98a" -e "b32...q1r"
```

Example:
`java -jar target/open-token-1.0.jar -i src/main/resources/sample.csv -t csv -o src/main/output.csv -h "HashingKey" -e "Secret-Encryption-Key-Goes-Here."`

#### Via Docker

Please run the following command in the same folder as the source CSV file:

```shell
docker run -v "$(pwd)":/app open-token -i <input-file> -t csv -o <output-file> -h "xb7...98a" -e "b32...q1r"
```

Example:
`docker run -v "$(pwd)":/app open-token -i src/main/resources/sample.csv -t csv -o src/main/output.csv -h "HashingKey" -e "Secret-Encryption-Key-Goes-Here."`

### Arguments

The driver accepts multiple command line arguments:

- `-t | --type`: This argument is used to specify the input/output file type. You can provide the file type as a string. Only `csv` is supported now.

- `-i | --input`: This argument is used to specify the input file path. You can provide the path to an input file containing the sample data for person matching.

- `-o | --output`: This argument is used to specify the output file path. The generated tokens will be written to this file.

- `-h | --hashingsecret`: This argument is used to specify the hashing secret for the `HMAC-SHA256` digest. The generated tokens are hashed using this digest.

- `-e | --encryptionkey`: This argument is used to specify the encryption key for the `AES-256` symmetric encryption. The generated tokes are encrypted using this key.

The encryption logic is: Base64(AES-Encrypt(HMAC-SHA256(Hex(Sha256(token-signature)))))

The input file (in csv format) must contain at least the following column names:

- RecordId
- FirstName
- LastName
- Gender
- PostalCode
- BirthDate
- SocialSecurityNumber
Note: commas are only used for separation of field values, not for within values.

The output file (in csv format) contains the following columns:

- RecordId
- TokenId
- Token

### Building

#### With Maven

Prerequisites:

- Java 11 SDK
- Maven 3.8.7

Run the following:

```shell
mvn clean install
```

The compiled jar can be found under ./target

#### With Docker

Prerequisites:

- Docker

Run the following:

```shell
docker build . -t open-token
```

This will build a local Docker image called `open-token`.

#### Generating `javadoc`

The `javadoc` for the library can be generated as following:

```shell
mvn clean javadoc:javadoc
```

The Java documentation is created in `./target/reports/apidocs`. Invoke by opening `./target/reports/apidocs/index.html` in your favorite browser.

## Overview of the library

This project, `open-token`, provides common utilities, models, and services used across the person matching system. It is designed to support the development of applications and services that require person matching capabilities, ensuring consistency and efficiency.

## Getting started

To use `open-token` in your project, follow these steps:

1. Add it as a dependency in your build configuration file. For Maven, add the following code to your `pom.xml`:

```xml
<dependency>
    <groupId>com.truveta.opentoken</groupId>
    <artifactId>open-token</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Import `open-token` in your Java code using the following import statement:

```java
import com.truveta.opentoken.tokens.*;
```

3. Start using the utilities, models, and services provided by `open-token` in your project. For example, you can use the `TokenGenerator` class to perform token generation operations:

```java
ArrayList<Map<String, String>> result = new ArrayList<>();

/* get a list of person attributes  */
List<Map<String,String>> data = reader.readAttributes();

/* process person one by one */
data.forEach(row -> {
    /* generate all tokens for one person */
    Map<String, String> tokens = tokenGenerator.getAllTokens(row);
    logger.info("Tokens: {}", tokens);

    List<String> tokenIds = new ArrayList<>(tokens.keySet());
    Collections.sort(tokenIds);

    /* add all the token for the person in result */
    for (String tokenId : tokenIds) {
        var rowResult = new HashMap<String, String>();
        rowResult.put("RecordId", row.get("RecordId"));
        rowResult.put("RuleId", tokenId);
        rowResult.put("Token", tokens.get(tokenId));
        result.add(rowResult);
    }
});

// result has tokens for all persons now.
```

## Test Data

In order to test, you have the option to generate mock person data in the expected format.

### Prerequisites

- Python3
- [faker](https://pypi.org/project/Faker/)

### Generating mock data

Under `src/test/resources/mockdata` you can find a python script that allows to generate fake random person data. You can run it as follows with pre-configured defaults:

```shell
./generate.sh 
```

You can modify the parameters when running the script directly. The script will repeat a percentage of the record values using a different record ID.

```shell
# python data_generator.py <number of records> <percentage of repeated records> <output file name>
python data_generator.py 100 0.05 test_data.csv
```

## Contribute

Truveta encourages contributions in the form of features, bug fixes, documentation updates, etc. Some of the areas in key needs of improvements are:

1. The library currently provides `csv` reader and writer. See `com.truveta.opentoken.io`. Readers/writers for `parquet` file is highly desired.
2. More test coverage.
