package root.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import root.configuration.properties.KafkaProducerProperties;
import root.dto.DataForProcessingMessage;
import root.dto.ProcessedDataMessage;
import root.service.DataProcessingService;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Configuration
@EnableConfigurationProperties(KafkaProducerProperties.class)
public class KafkaConfiguration {

    @Bean
    public Consumer<DataForProcessingMessage> dataProcessingFlow(DataProcessingService dataProcessingService) {
        return message -> Optional.ofNullable(message)
                .filter(DataForProcessingMessage::isValid)
                .map(DataForProcessingMessage::getDataForProcessing)
                .ifPresentOrElse(
                        dataProcessingService::process,
                        () -> log.warn("### Invalid message has been skipped: {}", message)
                );
    }

    @Bean
    public KafkaTemplate<String, ProcessedDataMessage> dataProcessingOutputKafkaProducer(KafkaProducerProperties kafkaProducerProperties) {
        return createKafkaProducer("data-processing-flow", JsonSerializer.class, kafkaProducerProperties);
    }

    private <T> KafkaTemplate<String, T> createKafkaProducer(
            String key,
            Class<?> valueSerializer,
            KafkaProducerProperties kafkaProducerProperties
    ) {
        var producerConfig = new HashMap<String, Object>(kafkaProducerProperties.getSettings());
        producerConfig.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, Boolean.FALSE);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        var producerFactory = new DefaultKafkaProducerFactory<String, T>(producerConfig);
        var topicName = kafkaProducerProperties.getTopic().get(key).getTopicName();
        var kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(topicName);
        return kafkaTemplate;
    }
}
