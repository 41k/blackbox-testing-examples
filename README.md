# Blackbox (functional) testing examples


## Description

This ```Spring Boot``` application shows blackbox testing approach for several commonly used data processing flows using the next technologies stack:
- ```Groovy Spock (package tests.groovy.*)``` and ```JUnit (package tests.java.*)``` are used as general unit-testing frameworks
- ```Rest Assured``` is used for performing HTTP requests to the application under the test and assertion of responses
- ```Wiremock``` is used for mocking of communication via HTTP with 3rd party services
- ```Testcontainers``` framework is used for infrastructure establishment (Database, Kafka broker etc.) which is used during testing



## Flow 1

Blackbox tests for the flow can be found in ```Flow1BlackboxTest```

### Steps:
1. Application receives ```DataProcessingRequest``` via HTTP endpoint ```POST /api/v1/processing```
2. Application makes ```GET /additional-data/{id}``` HTTP-request to 3rd party service in order to obtain appropriate additional data
3. Application forms ```ProcessedData``` item and saves it into Database (MariaDB)
4. Application sends ```ProcessedDataMessage``` to Kafka topic ```topic-b``` for further data processing



## Flow 2

Blackbox tests for the flow can be found in ```Flow2BlackboxTest```

### Steps:
1. Application reads ```DataForProcessingMessage``` from Kafka topic ```topic-a```
2. Application makes ```GET /additional-data/{id}``` HTTP-request to 3rd party service in order to obtain appropriate additional data
3. Application forms ```ProcessedData``` item and saves it into Database (MariaDB)
4. Application sends ```ProcessedDataMessage``` to Kafka topic ```topic-b``` for further data processing



## Cache

Test example for cache functionality can be found in ```ThirdPartyServiceClientCacheTest```