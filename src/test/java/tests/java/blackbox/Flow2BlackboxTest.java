package tests.java.blackbox;

import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static tests.java.TestConstants.*;

public class Flow2BlackboxTest extends BaseBlackboxTest {

    private static final String DATA_FOR_PROCESSING_MESSAGE = """
                {
                    "data": "%s",
                    "additionalDataId": "%s"
                }
            """.formatted(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Value("${spring.cloud.stream.bindings.dataProcessingFlow-in-0.destination}")
    private String dataProcessingFlowInputTopic;

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
        // message with data for processing is coming
        kafkaProducer.send(new ProducerRecord<String, String>(dataProcessingFlowInputTopic, null, DATA_FOR_PROCESSING_MESSAGE));

        // THEN:
        // processed data is persisted
        Awaitility.await()
                .atMost(10, SECONDS)
                .with()
                .pollInterval(1, SECONDS)
                .untilAsserted(() ->
                        assertThat(processedDataRepository.findAll())
                                .hasSize(1)
                                .contains(PROCESSED_DATA));
        // processed data is sent to output kafka topic for further processing
        dataProcessingOutputKafkaConsumer.waitAndAssertMessagesSent(List.of(PROCESSED_DATA_MESSAGE));
    }

    @Test
    @SneakyThrows
    void shouldSkipInvalidMessageAndPerformNoProcessing() {
        // GIVEN:
        // no data has been processed yet
        assertThat(processedDataRepository.findAll()).isEmpty();

        // WHEN:
        // message with data for processing is coming
        kafkaProducer.send(new ProducerRecord<String, String>(dataProcessingFlowInputTopic, null, "{}"));

        // AND:
        SECONDS.sleep(2);

        // THEN:
        // no processed data is persisted
        assertThat(processedDataRepository.findAll()).isEmpty();
        // no processed data is sent to output kafka topic for further processing
        dataProcessingOutputKafkaConsumer.assertNoMessageSent();
    }
}
