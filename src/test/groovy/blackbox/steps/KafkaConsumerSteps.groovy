package blackbox.steps

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.skyscreamer.jsonassert.JSONCompareMode

import static TestConstants.*
import static TestUtils.*

class KafkaConsumerSteps implements Closeable {

    KafkaConsumer<String, String> consumer

    KafkaConsumerSteps(String topic, String brokers) {
        consumer = createKafkaConsumer(topic, brokers)
    }

    void waitAndAssertMessageSent(String body, JSONCompareMode mode = JSONCompareMode.LENIENT) {
        println "waitAndAssertMessageSent: expected [$body]"
        def found = false
        POLLING_CONDITIONS.eventually {
            def fetchedRecords = waitAndReceiveRecords(consumer, 1)
            for (record in fetchedRecords) {
                println "found message [$record]"
                found = matchJson(body, record.getValue(), mode)
                if (found) {
                    println 'Message matches expectation'
                    break
                }
                println 'Message does not match expected'
                throw new RuntimeException('Message does not match expected')
            }
        }
        assert found
    }

    void waitAndAssertMessagesSent(List<String> bodies, JSONCompareMode mode = JSONCompareMode.LENIENT) {
        def records = waitAndReceiveRecords(consumer, bodies.size())
        for (int i = 0; i < bodies.size(); i++) {
            def foundMatching = false
            for (int j = 0; j < records.size(); j++) {
                def expected = bodies[i]
                def actual = records[j].getValue()
                foundMatching = expected.equals(actual) || matchJson(expected, actual, mode)
                if (foundMatching) {
                    break
                }
            }
            assert foundMatching : "expected messages [$bodies], but actual received messages [$records]"
        }
    }

    void assertNoMessageSent() {
        assertNoMessageSent(consumer)
    }

    void cleanTopic() {
        cleanTopic(consumer)
    }

    KafkaConsumer<String, String> getConsumer() {
        consumer
    }

    @Override
    void close() {
        consumer.close()
    }
}
