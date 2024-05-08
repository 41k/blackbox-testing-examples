package tests.groovy.unit.dto

import root.dto.DataProcessingRequest
import spock.lang.Specification

import static tests.groovy.TestConstants.*

class DataProcessingRequestTest extends Specification {

    def 'should provide data for processing'() {
        given:
        def request = new DataProcessingRequest(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID)

        expect:
        request.getDataForProcessing() == DATA_FOR_PROCESSING
    }
}
