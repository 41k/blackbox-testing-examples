package unit.dto

import root.dto.DataProcessingRequest
import spock.lang.Specification

import static TestConstants.*

class DataProcessingRequestTest extends Specification {

    private static final DATA_PROCESSING_REQUEST =
            new DataProcessingRequest(data: DATA_FOR_PROCESSING_AS_STRING, additionalDataId: ADDITIONAL_DATA_ID)

    def 'should provide data for processing'() {
        expect:
        DATA_PROCESSING_REQUEST.getDataForProcessing() == DATA_FOR_PROCESSING
    }
}
