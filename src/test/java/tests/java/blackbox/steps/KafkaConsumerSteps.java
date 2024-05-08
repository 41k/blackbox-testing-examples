package tests.java.blackbox.steps;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.awaitility.Awaitility;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;
import static tests.java.TestConstants.FETCH_TIMEOUT_MILLIS;

@RequiredArgsConstructor
public class KafkaConsumerSteps implements Closeable {

    private final KafkaConsumer<String, String> consumer;

    @Override
    public void close() {
        consumer.close();
    }

    public void waitAndAssertMessagesSent(List<Pair<String, String>> expectedMessages) {
        var actualMessages = waitAndReceiveMessages(consumer, expectedMessages.size());
        for (int i = 0; i < expectedMessages.size(); i++) {
            var foundMatching = false;
            for (int j = 0; j < actualMessages.size(); j++) {
                var expectedMessage = expectedMessages.get(i);
                var actualMessage = actualMessages.get(i);
                foundMatching = expectedMessage.getKey().equals(actualMessage.getKey()) &&
                        (expectedMessage.getValue().equals(actualMessage.getValue()) ||
                                matchJson(expectedMessage.getValue(), actualMessage.getValue()));
                if (foundMatching) {
                    break;
                }
            }
            assert foundMatching :
                    "Expected messages are " + expectedMessages + ", but received messages are " + actualMessages;
        }
    }

    public void assertNoMessageSent() {
        assert getRecords(consumer, FETCH_TIMEOUT_MILLIS).count() == 0;
    }

    public void cleanTopic() {
        getRecords(consumer, FETCH_TIMEOUT_MILLIS);
    }

    private boolean matchJson(String expected, String actual) {
        try {
            return JSONCompare.compareJSON(expected, actual, JSONCompareMode.LENIENT).passed();
        } catch (JSONException e) {
            return false;
        }
    }

    private <K, V> List<Pair<K, V>> waitAndReceiveMessages(KafkaConsumer<K, V> consumer, int nMessages) {
        var consumedMessages = new ArrayList<Pair<K, V>>();
        Awaitility.await()
                .atMost(10, SECONDS)
                .with()
                .pollInterval(1, SECONDS)
                .until(() -> {
                    getRecords(consumer, FETCH_TIMEOUT_MILLIS).forEach(record ->
                            consumedMessages.add(Pair.of(record.key(), record.value())));
                    return consumedMessages.size() >= nMessages;
                });
        return consumedMessages;
    }
}
