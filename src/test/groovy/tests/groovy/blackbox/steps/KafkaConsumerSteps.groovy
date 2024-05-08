package tests.groovy.blackbox.steps

import org.apache.commons.lang3.tuple.Pair
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.json.JSONException
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode

import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords
import static tests.groovy.TestConstants.FETCH_TIMEOUT_MILLIS
import static tests.groovy.TestConstants.POLLING_CONDITIONS

class KafkaConsumerSteps implements Closeable {

    KafkaConsumer<String, String> consumer

    KafkaConsumerSteps(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer
    }

    @Override
    void close() {
        consumer.close()
    }

    void waitAndAssertMessagesSent(List<Pair<String, String>> expectedMessages) {
        def actualMessages = waitAndReceiveMessages(consumer, expectedMessages.size())
        for (int i = 0; i < expectedMessages.size(); i++) {
            def foundMatching = false
            for (int j = 0; j < actualMessages.size(); j++) {
                def expectedMessage = expectedMessages[i]
                def actualMessage = actualMessages[j]
                foundMatching = expectedMessage.getKey().equals(actualMessage.getKey()) &&
                        (expectedMessage.getValue().equals(actualMessage.getValue()) ||
                                matchJson(expectedMessage.getValue(), actualMessage.getValue()))
                if (foundMatching) {
                    break
                }
            }
            assert foundMatching : "Expected messages are $expectedMessages, but received messages are $actualMessages"
        }
    }

    void assertNoMessageSent() {
        assert getRecords(consumer, FETCH_TIMEOUT_MILLIS).count() == 0
    }

    void cleanTopic() {
        getRecords(consumer, FETCH_TIMEOUT_MILLIS)
    }

    private <K, V> List<Pair<K, V>> waitAndReceiveMessages(KafkaConsumer<K, V> consumer, int nMessages) {
        def consumedMessages = []
        POLLING_CONDITIONS.eventually {
            def records = getRecords(consumer, FETCH_TIMEOUT_MILLIS)
            records?.each {
                consumedMessages << Pair.of(it.key(), it.value())
            }
            assert consumedMessages.size() >= nMessages
        }
        consumedMessages
    }

    private boolean matchJson(String expected, String actual) {
        try {
            JSONCompare.compareJSON(expected, actual, JSONCompareMode.LENIENT).passed()
        } catch (JSONException e) {
            false
        }
    }
}
