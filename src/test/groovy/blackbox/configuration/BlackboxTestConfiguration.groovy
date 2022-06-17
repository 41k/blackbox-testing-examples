package blackbox.configuration

import blackbox.steps.KafkaConsumerSteps
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import root.configuration.properties.KafkaProducerProperties

import java.time.Clock

import static TestConstants.CLOCK
import static TestUtils.createKafkaProducer

@TestConfiguration
@EnableConfigurationProperties(KafkaProducerProperties)
class BlackboxTestConfiguration {

    @Value('${embedded.kafka.brokerList}')
    String brokerList

    @Bean
    KafkaProducer kafkaProducer() {
        createKafkaProducer(brokerList)
    }

    @Bean(destroyMethod = 'close')
    KafkaConsumerSteps dataProcessingOutputKafkaConsumer(KafkaProducerProperties kafkaProducerProperties) {
        def topicName = kafkaProducerProperties.getTopic().get('data-processing-flow').getTopicName()
        new KafkaConsumerSteps(topicName, brokerList)
    }

    @Bean
    Clock clock() {
        CLOCK
    }
}
