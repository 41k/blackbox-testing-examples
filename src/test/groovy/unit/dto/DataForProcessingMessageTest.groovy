package unit.dto

import root.dto.DataForProcessingMessage
import spock.lang.Specification

import static TestConstants.*

class DataForProcessingMessageTest extends Specification {

    private static final DATA_FOR_PROCESSING_MESSAGE =
            new DataForProcessingMessage(data: DATA_FOR_PROCESSING_AS_STRING, additionalDataId: ADDITIONAL_DATA_ID)

    def 'should validate itself'() {
        expect:
        DATA_FOR_PROCESSING_MESSAGE.isValid()

        and:
        !new DataForProcessingMessage().isValid()
    }

    def 'should provide data for processing'() {
        expect:
        DATA_FOR_PROCESSING_MESSAGE.getDataForProcessing() == DATA_FOR_PROCESSING
    }
}
