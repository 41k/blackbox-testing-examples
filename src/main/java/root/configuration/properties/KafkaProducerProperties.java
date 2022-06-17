package root.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties("kafka.producers")
public class KafkaProducerProperties {

    @NotEmpty
    private Map<String, KafkaTopicSettings> topic;

    private Map<String, String> settings = new HashMap<>();

    @Data
    @Validated
    public static class KafkaTopicSettings {
        @NotNull
        private String topicName;
    }
}
