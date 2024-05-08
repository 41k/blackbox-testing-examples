package root.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;
import java.util.Optional;

@Data
@Validated
@ConfigurationProperties("cache")
public class CacheProperties {

    public static final String ADDITIONAL_DATA_CACHE_NAME = "additional-data";

    @NotEmpty
    private Map<String, @PositiveOrZero Long> durationInSeconds;

    public Long getDurationInSeconds(String cacheName) {
        return Optional.ofNullable(durationInSeconds.get(cacheName)).orElseThrow();
    }
}
