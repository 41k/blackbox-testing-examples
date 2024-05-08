package tests.groovy.blackbox

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.apache.http.HttpStatus.SC_OK
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static tests.groovy.TestConstants.*

class Flow2BlackboxTest extends BaseBlackboxTest {

    private static final DATA_FOR_PROCESSING_MESSAGE = """
        {
            "data": "$DATA_FOR_PROCESSING_AS_STRING",
            "additionalDataId": "$ADDITIONAL_DATA_ID"
        }
    """ as String

    @Autowired
    private KafkaProducer kafkaProducer

    @Value('${spring.cloud.stream.bindings.dataProcessingFlow-in-0.destination}')
    String dataProcessingFlowInputTopic

    def 'should process data successfully'() {
        given: 'no data has been processed yet'
        assert processedDataRepository.findAll().isEmpty()

        and: 'mock for successful additional data retrieval from third-party service'
        stubFor(get(urlPathEqualTo(THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URI))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(THIRD_PARTY_SERVICE_RESPONSE_BODY)))

        when: 'message with data for processing is coming'
        kafkaProducer.send(new ProducerRecord<String, String>(dataProcessingFlowInputTopic, null, DATA_FOR_PROCESSING_MESSAGE))

        then: 'processed data is persisted'
        POLLING_CONDITIONS.eventually {
            def processedData = processedDataRepository.findAll()
            assert processedData.size() == 1
            assert processedData.get(0).equals(PROCESSED_DATA)
        }

        and: 'processed data is sent to output kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.waitAndAssertMessagesSent([PROCESSED_DATA_MESSAGE])
    }

    def 'should skip invalid message and perform no processing'() {
        given: 'no data has been processed yet'
        assert processedDataRepository.findAll().isEmpty()

        when: 'message with data for processing is coming'
        kafkaProducer.send(new ProducerRecord<String, String>(dataProcessingFlowInputTopic, null, '{}'))

        and:
        sleep(2000L)

        then: 'no processed data is persisted'
        processedDataRepository.findAll().isEmpty()

        and: 'no processed data is sent to output kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.assertNoMessageSent()
    }
}
