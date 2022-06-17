import org.apache.commons.lang3.tuple.Pair
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.json.JSONException
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode

import static TestConstants.FETCH_TIMEOUT_MS
import static TestConstants.POLLING_CONDITIONS
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords

class TestUtils {

    static KafkaProducer createKafkaProducer(String brokers) {
        def config = [(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)     : brokers,
                      (ProducerConfig.CLIENT_ID_CONFIG)             : 'kafka-producer-1',
                      (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)  : StringSerializer,
                      (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG): StringSerializer]
        new KafkaProducer<>(config)
    }

    static KafkaConsumer<String, String> createKafkaConsumer(String topic, String brokers) {
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

    static boolean matchJson(String expected, String actual, JSONCompareMode mode) {
        try {
            JSONCompare.compareJSON(expected, actual, mode).passed()
        } catch (JSONException e) {
            false
        }
    }

    static <K, V> List<Pair<K, V>> waitAndReceiveRecords(KafkaConsumer<K, V> consumer, int minMessages) {
        def consumedValues = []
        POLLING_CONDITIONS.eventually {
            def records = getRecords(consumer, FETCH_TIMEOUT_MS)
            records?.each {
                consumedValues << Pair.of(it.key(), (it.value()))
            }
            assert consumedValues.size() >= minMessages
        }
        consumedValues
    }

    static <K, V> void assertNoMessageSent(KafkaConsumer<K, V> consumer) {
        assert getRecords(consumer, FETCH_TIMEOUT_MS).size() == 0
    }

    static <K, V> void cleanTopic(KafkaConsumer<K, V> consumer) {
        getRecords(consumer, FETCH_TIMEOUT_MS)
    }
}
