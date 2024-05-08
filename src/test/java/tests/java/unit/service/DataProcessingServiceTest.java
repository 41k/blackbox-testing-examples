package tests.java.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import root.repository.ProcessedDataRepository;
import root.service.DataProcessingService;
import root.service.IdGenerator;
import root.service.ThirdPartyServiceClient;

import static org.mockito.Mockito.*;
import static tests.java.TestConstants.*;

@ExtendWith(MockitoExtension.class)
public class DataProcessingServiceTest {

    @Mock
    private IdGenerator idGenerator;
    @Mock
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Mock
    private ProcessedDataRepository processedDataRepository;
    @Mock
    private KafkaTemplate dataProcessingOutputKafkaProducer;

    private DataProcessingService dataProcessingService;

    @BeforeEach
    void setUp() {
        dataProcessingService = new DataProcessingService(
                idGenerator, CLOCK, thirdPartyServiceClient,
                processedDataRepository, dataProcessingOutputKafkaProducer);
    }

    @Test
    void shouldProcessData() {
        // GIVEN:
        when(thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID)).thenReturn(ADDITIONAL_DATA);
        when(idGenerator.generate()).thenReturn(PROCESSED_DATA_ID);

        // WHEN:
        dataProcessingService.process(DATA_FOR_PROCESSING);

        // THEN:
        verify(thirdPartyServiceClient).getAdditionalData(ADDITIONAL_DATA_ID);
        verify(idGenerator).generate();
        verify(processedDataRepository).save(PROCESSED_DATA);
        verify(dataProcessingOutputKafkaProducer).sendDefault(PROCESSED_DATA_ID, PROCESSED_DATA_MESSAGE_VALUE);
        verifyNoMoreInteractions(
                thirdPartyServiceClient,
                idGenerator,
                processedDataRepository,
                dataProcessingOutputKafkaProducer);
    }
}
