package root.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import root.dto.ProcessedDataMessage;
import root.model.DataForProcessing;
import root.model.ProcessedData;
import root.repository.ProcessedDataRepository;

import java.time.Clock;

@RequiredArgsConstructor
public class DataProcessingService {

    private final IdGenerator idGenerator;
    private final Clock clock;
    private final ThirdPartyServiceClient thirdPartyServiceClient;
    private final ProcessedDataRepository processedDataRepository;
    private final KafkaTemplate<String, ProcessedDataMessage> dataProcessingOutputKafkaProducer;

    @Transactional
    public void process(DataForProcessing dataForProcessing) {
        var additionalData = thirdPartyServiceClient.getAdditionalData(dataForProcessing.getAdditionalDataId());
        var processedData = dataForProcessing.getData().toUpperCase() + " | " + additionalData.toUpperCase();
        processedDataRepository.save(
                ProcessedData.builder()
                        .id(idGenerator.generate())
                        .data(processedData)
                        .processingTimestamp(clock.instant())
                        .build());
        dataProcessingOutputKafkaProducer.sendDefault(new ProcessedDataMessage(processedData));
    }
}
