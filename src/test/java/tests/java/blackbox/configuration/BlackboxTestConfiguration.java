package tests.java.blackbox.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import root.configuration.properties.KafkaProducerProperties;
import root.service.IdGenerator;
import tests.java.blackbox.steps.KafkaConsumerSteps;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;

import static tests.java.TestConstants.CLOCK;
import static tests.java.TestConstants.PROCESSED_DATA_ID;

@TestConfiguration
@EnableConfigurationProperties(KafkaProducerProperties.class)
public class BlackboxTestConfiguration {

    @Value("${embedded.kafka.brokerList}")
    private String brokerList;

    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator() {
            @Override
            public String generate() {
                return PROCESSED_DATA_ID;
            }
        };
    }

    @Bean
    public Clock clock() {
        return CLOCK;
    }

    @Bean
    public KafkaProducer kafkaProducer() {
        return createKafkaProducer(brokerList);
    }

    @Bean(destroyMethod = "close")
    public KafkaConsumerSteps dataProcessingOutputKafkaConsumer(KafkaProducerProperties kafkaProducerProperties) {
        var topic = kafkaProducerProperties.getTopic().get("data-processing-flow").getTopicName();
        var consumer = createKafkaConsumer(topic, brokerList);
        return new KafkaConsumerSteps(consumer);
    }

    private KafkaProducer createKafkaProducer(String brokers) {
        var config = new HashMap<String, Object>() {{
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
            put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-producer-1");
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        }};
        return new KafkaProducer<>(config);
    }

    private KafkaConsumer<String, String> createKafkaConsumer(String topic, String brokers) {
        var config = new HashMap<String, Object>() {{
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-group[" + topic + "]");
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        }};
        var kafkaConsumer = new KafkaConsumer(config);
        kafkaConsumer.subscribe(List.of(topic));
        return kafkaConsumer;
    }
}
