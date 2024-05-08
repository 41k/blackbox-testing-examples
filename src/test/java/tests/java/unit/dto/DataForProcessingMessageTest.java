package tests.java.unit.dto;

import org.junit.jupiter.api.Test;
import root.dto.DataForProcessingMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.java.TestConstants.*;

public class DataForProcessingMessageTest {

    private static final DataForProcessingMessage DATA_FOR_PROCESSING_MESSAGE =
            new DataForProcessingMessage(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID);

    @Test
    void shouldValidateItself() {
        assertThat(DATA_FOR_PROCESSING_MESSAGE.isValid()).isTrue();
        assertThat(new DataForProcessingMessage(DATA_FOR_PROCESSING_AS_STRING, null).isValid()).isFalse();
        assertThat(new DataForProcessingMessage(null, ADDITIONAL_DATA_ID).isValid()).isFalse();
    }

    @Test
    void shouldProvideDataForProcessing() {
        assertThat(DATA_FOR_PROCESSING_MESSAGE.getDataForProcessing()).isEqualTo(DATA_FOR_PROCESSING);
    }
}
