package blackbox

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import static TestConstants.*
import static com.github.tomakehurst.wiremock.client.WireMock.*

class Flow2BlackboxTest extends BaseBlackboxTest {

    private static final DATA_FOR_PROCESSING_MESSAGE = """
        {
            "data": "$DATA_FOR_PROCESSING_AS_STRING",
            "additionalDataId": "$ADDITIONAL_DATA_ID"
        }
    """ as String

    @Autowired
    KafkaProducer kafkaProducer

    @Value('${spring.cloud.stream.bindings.dataProcessingFlow-in-0.destination}')
    String dataProcessingFlowInputTopic

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

        when: 'message with data for processing is coming'
        kafkaProducer.send(new ProducerRecord<String, String>(dataProcessingFlowInputTopic, null, DATA_FOR_PROCESSING_MESSAGE))

        then: 'processed data is persisted'
        POLLING_CONDITIONS.eventually {
            def processedData = processedDataRepository.findAll()
            assert processedData.size() == 1
            assert processedData.get(0).data == PROCESSED_DATA_AS_STRING
            assert processedData.get(0).processingTimestamp == PROCESSING_TIMESTAMP
        }

        and: 'processed data is sent to subsequent kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.waitAndAssertMessageSent(PROCESSED_DATA_MESSAGE_AS_JSON)
    }

    def 'should skip invalid message and perform no processing'() {
        given: 'no data has been processed yet'
        assert processedDataRepository.findAll().isEmpty()

        when: 'message with data for processing is coming'
        kafkaProducer.send(new ProducerRecord<String, String>(dataProcessingFlowInputTopic, null, '{}'))

        and:
        sleep(1000L)

        then: 'no processed data is persisted'
        processedDataRepository.findAll().isEmpty()

        and: 'no processed data is sent to subsequent kafka topic for further processing'
        dataProcessingOutputKafkaConsumer.assertNoMessageSent()
    }
}
