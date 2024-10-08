package tests.groovy.unit.dto

import root.dto.DataForProcessingMessage
import spock.lang.Specification

import static tests.groovy.TestConstants.*

class DataForProcessingMessageTest extends Specification {

    private static final DATA_FOR_PROCESSING_MESSAGE =
            new DataForProcessingMessage(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID)

    def 'should validate itself'() {
        expect:
        DATA_FOR_PROCESSING_MESSAGE.isValid()

        and:
        !new DataForProcessingMessage(DATA_FOR_PROCESSING_AS_STRING, null).isValid()

        and:
        !new DataForProcessingMessage(null, ADDITIONAL_DATA_ID).isValid()
    }

    def 'should provide data for processing'() {
        expect:
        DATA_FOR_PROCESSING_MESSAGE.getDataForProcessing() == DATA_FOR_PROCESSING
    }
}
