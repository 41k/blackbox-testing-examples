package tests.groovy.blackbox.configuration

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import root.configuration.properties.KafkaProducerProperties
import root.service.IdGenerator
import tests.groovy.blackbox.steps.KafkaConsumerSteps

import java.time.Clock

import static tests.groovy.TestConstants.CLOCK
import static tests.groovy.TestConstants.PROCESSED_DATA_ID

@TestConfiguration
@EnableConfigurationProperties(KafkaProducerProperties)
class BlackboxTestConfiguration {

    @Value('${embedded.kafka.brokerList}')
    String brokerList

    @Bean
    IdGenerator idGenerator() {
        new IdGenerator() {
            @Override
            String generate() {
                PROCESSED_DATA_ID
            }
        }
    }

    @Bean
    Clock clock() {
        CLOCK
    }

    @Bean
    KafkaProducer kafkaProducer() {
        createKafkaProducer(brokerList)
    }

    @Bean(destroyMethod = 'close')
    KafkaConsumerSteps dataProcessingOutputKafkaConsumer(KafkaProducerProperties kafkaProducerProperties) {
        def topic = kafkaProducerProperties.getTopic().get('data-processing-flow').getTopicName()
        def consumer = createKafkaConsumer(topic, brokerList)
        new KafkaConsumerSteps(consumer)
    }

    private KafkaProducer createKafkaProducer(String brokers) {
        def config = [(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)     : brokers,
                      (ProducerConfig.CLIENT_ID_CONFIG)             : 'kafka-producer-1',
                      (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)  : StringSerializer,
                      (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG): StringSerializer]
        new KafkaProducer<>(config)
    }

    private KafkaConsumer<String, String> createKafkaConsumer(String topic, String brokers) {
        def config = [(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)       : brokers,
                      (ConsumerConfig.GROUP_ID_CONFIG)                : 'test-group[' + topic + ']',
                      (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest',
                      (ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)  : StringDeserializer,
                      (ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG): StringDeserializer,
                      (ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG) : '1000']

        def kafkaConsumer = new KafkaConsumer(config)
        kafkaConsumer.subscribe([topic])
        kafkaConsumer
    }
}
