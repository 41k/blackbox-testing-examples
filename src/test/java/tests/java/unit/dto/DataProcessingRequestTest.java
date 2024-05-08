package tests.java.unit.dto;

import org.junit.jupiter.api.Test;
import root.dto.DataProcessingRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.java.TestConstants.*;

public class DataProcessingRequestTest {

    @Test
    void shouldProvideDataForProcessing() {
        // GIVEN:
        var request = new DataProcessingRequest(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID);

        // EXPECT:
        assertThat(request.getDataForProcessing()).isEqualTo(DATA_FOR_PROCESSING);
    }
}
