package tests.groovy.unit.service

import org.springframework.kafka.core.KafkaTemplate
import root.repository.ProcessedDataRepository
import root.service.DataProcessingService
import root.service.IdGenerator
import root.service.ThirdPartyServiceClient
import spock.lang.Specification

import static tests.groovy.TestConstants.*

class DataProcessingServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private thirdPartyServiceClient = Mock(ThirdPartyServiceClient)
    private processedDataRepository = Mock(ProcessedDataRepository)
    private dataProcessingOutputKafkaProducer = Mock(KafkaTemplate)

    private dataProcessingService = new DataProcessingService(
            idGenerator, CLOCK, thirdPartyServiceClient, processedDataRepository, dataProcessingOutputKafkaProducer)

    def 'should process data'() {
        when:
        dataProcessingService.process(DATA_FOR_PROCESSING)

        then:
        1 * thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID) >> ADDITIONAL_DATA
        1 * idGenerator.generate() >> PROCESSED_DATA_ID
        1 * processedDataRepository.save(PROCESSED_DATA)
        1 * dataProcessingOutputKafkaProducer.sendDefault(PROCESSED_DATA_ID, PROCESSED_DATA_MESSAGE_VALUE)
        0 * _
    }
}
