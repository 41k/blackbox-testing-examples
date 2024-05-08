package tests.groovy.blackbox

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static io.restassured.RestAssured.given
import static org.apache.http.HttpStatus.*
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static tests.groovy.TestConstants.*

class Flow1BlackboxTest extends BaseBlackboxTest {

    private static final DATA_PROCESSING_REQUEST_BODY = """
        {
            "data": "$DATA_FOR_PROCESSING_AS_STRING",
            "additionalDataId": "$ADDITIONAL_DATA_ID"
        }
    """

    def 'should process data successfully'() {
        given: 'no data has been processed yet'
        assert processedDataRepository.findAll().isEmpty()

        and: 'mock for successful additional data retrieval from third-party service'
        stubFor(get(urlPathEqualTo(THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URI))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(THIRD_PARTY_SERVICE_RESPONSE_BODY)))

        when: 'data processing request is sent'
        given().contentType(APPLICATION_JSON_VALUE)
                .body(DATA_PROCESSING_REQUEST_BODY)
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_OK)

        then: 'processed data is persisted'
        def processedData = processedDataRepository.findAll()
        processedData.size() == 1
        processedData.get(0).equals(PROCESSED_DATA)

        and: 'processed data is sent to output kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.waitAndAssertMessagesSent([PROCESSED_DATA_MESSAGE])
    }

    def 'should get 400 response code for incorrect data processing request'() {
        expect:
        given().contentType(APPLICATION_JSON_VALUE)
                .body('{}')
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_BAD_REQUEST)
    }

    def 'should fail data processing if third party service failed to provide additional data'() {
        given: 'no data has been processed yet'
        assert processedDataRepository.findAll().isEmpty()

        and: 'mock for failed additional data retrieval from third-party service'
        stubFor(get(urlPathEqualTo(THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URI))
                .willReturn(aResponse()
                        .withStatus(SC_INTERNAL_SERVER_ERROR)))

        when: 'data processing request is sent'
        given().contentType(APPLICATION_JSON_VALUE)
                .body(DATA_PROCESSING_REQUEST_BODY)
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR)

        then: 'no processed data is persisted'
        processedDataRepository.findAll().isEmpty()

        and: 'no processed data is sent to output kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.assertNoMessageSent()
    }
}
