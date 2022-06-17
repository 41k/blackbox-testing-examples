package blackbox

import static TestConstants.*
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static io.restassured.RestAssured.given
import static org.apache.http.HttpStatus.*

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
                        .withStatus(200)
                        .withBody(THIRD_PARTY_SERVICE_RESPONSE_BODY)
                        .withHeader('Content-Type', JSON_CONTENT_TYPE)
                        .withHeader('Connection', 'close')))

        when: 'data processing request is sent'
        given().contentType(JSON_CONTENT_TYPE)
                .body(DATA_PROCESSING_REQUEST_BODY)
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_OK)

        then: 'processed data is persisted'
        def processedData = processedDataRepository.findAll()
        processedData.size() == 1
        processedData.get(0).data == PROCESSED_DATA_AS_STRING
        processedData.get(0).processingTimestamp == PROCESSING_TIMESTAMP

        and: 'processed data is sent to subsequent kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.waitAndAssertMessageSent(PROCESSED_DATA_MESSAGE_AS_JSON)
    }

    def 'should get 400 response code for incorrect data processing request'() {
        expect:
        given().contentType(JSON_CONTENT_TYPE)
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
                        .withStatus(500)
                        .withHeader('Connection', 'close')))

        when: 'data processing request is sent'
        given().contentType(JSON_CONTENT_TYPE)
                .body(DATA_PROCESSING_REQUEST_BODY)
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR)

        then: 'no processed data is persisted'
        processedDataRepository.findAll().isEmpty()

        and: 'no processed data is sent to subsequent kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.assertNoMessageSent()
    }
}
