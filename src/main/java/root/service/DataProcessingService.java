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
        var additionalDataId = dataForProcessing.getAdditionalDataId();
        var additionalData = thirdPartyServiceClient.getAdditionalData(additionalDataId);
        var rawData = dataForProcessing.getData();
        var processedData = rawData.toUpperCase() + " | " + additionalData.toUpperCase();
        var processedDataId = idGenerator.generate();
        processedDataRepository.save(
                ProcessedData.builder()
                        .id(processedDataId)
                        .dataForProcessing(rawData)
                        .additionalDataId(additionalDataId)
                        .processingResult(processedData)
                        .processingTimestamp(clock.instant())
                        .build());
        dataProcessingOutputKafkaProducer.sendDefault(processedDataId, new ProcessedDataMessage(processedData));
    }
}
