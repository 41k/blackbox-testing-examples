package root.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Validated
@ConfigurationProperties("thirdparty-service")
public class ThirdPartyServiceProperties {
    @NotBlank
    private String url;
    @Min(1000)
    public int connectionTimeoutMillis;
    @Min(1000)
    public int readTimeoutMillis;
    @Min(1)
    private int retryMax;
    @Min(1)
    private int retryBackoffMillis;
}
