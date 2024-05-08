package tests.java.blackbox;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static tests.java.TestConstants.*;

public class Flow1BlackboxTest extends BaseBlackboxTest {

    private static final String DATA_PROCESSING_REQUEST_BODY = """
                {
                    "data": "%s",
                    "additionalDataId": "%s"
                }
            """.formatted(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID);

    @Test
    void shouldProcessDataSuccessfully() {
        // GIVEN:
        // no data has been processed yet
        assertThat(processedDataRepository.findAll()).isEmpty();
        // mock for successful additional data retrieval from third-party service
        stubFor(get(urlPathEqualTo(THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URI))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(THIRD_PARTY_SERVICE_RESPONSE_BODY)));

        // WHEN:
        // data processing request is sent
        given().contentType(APPLICATION_JSON_VALUE)
                .body(DATA_PROCESSING_REQUEST_BODY)
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_OK);

        // THEN:
        // processed data is persisted
        assertThat(processedDataRepository.findAll())
                .hasSize(1)
                .contains(PROCESSED_DATA);
        // processed data is sent to output kafka topic for further processing
        dataProcessingOutputKafkaConsumer.waitAndAssertMessagesSent(List.of(PROCESSED_DATA_MESSAGE));
    }

    @Test
    void shouldGet400ResponseCodeForIncorrectDataProcessingRequest() {
        // EXPECT:
        given().contentType(APPLICATION_JSON_VALUE)
                .body("{}")
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_BAD_REQUEST);
    }

    @Test
    void shouldFailDataProcessingIfThirdPartyServiceFailedToProvideAdditionalData() {
        // GIVEN:
        // no data has been processed yet
        assertThat(processedDataRepository.findAll()).isEmpty();
        // mock for failed additional data retrieval from third-party service
        stubFor(get(urlPathEqualTo(THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URI))
                .willReturn(aResponse()
                        .withStatus(SC_INTERNAL_SERVER_ERROR)));

        // WHEN:
        // data processing request is sent
        given().contentType(APPLICATION_JSON_VALUE)
                .body(DATA_PROCESSING_REQUEST_BODY)
                .when()
                .post(DATA_PROCESSING_URL)
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);

        // THEN:
        // no processed data is persisted
        assertThat(processedDataRepository.findAll()).isEmpty();
        // no processed data is sent to output kafka topic for further processing
        dataProcessingOutputKafkaConsumer.assertNoMessageSent();
    }
}
