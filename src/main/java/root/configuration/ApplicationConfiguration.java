package root.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import root.configuration.properties.ThirdPartyServiceProperties;
import root.dto.ProcessedDataMessage;
import root.repository.ProcessedDataRepository;
import root.service.DataProcessingService;
import root.service.IdGenerator;
import root.service.ThirdPartyServiceClient;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;

import static java.lang.Boolean.TRUE;

@Configuration
@EnableConfigurationProperties(ThirdPartyServiceProperties.class)
public class ApplicationConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public DataProcessingService dataProcessingService(
            IdGenerator idGenerator,
            Clock clock,
            ThirdPartyServiceClient thirdPartyServiceClient,
            ProcessedDataRepository processedDataRepository,
            KafkaTemplate<String, ProcessedDataMessage> dataProcessingOutputKafkaProducer
    ) {
        return new DataProcessingService(
                idGenerator, clock, thirdPartyServiceClient, processedDataRepository, dataProcessingOutputKafkaProducer);
    }

    @Bean
    public ThirdPartyServiceClient thirdPartyServiceClient(
            ThirdPartyServiceProperties properties,
            RestTemplate thirdPartyServiceRestTemplate
    ) {
        var retryTemplate = createRetryTemplate(properties);
        return new ThirdPartyServiceClient(properties.getUrl(), retryTemplate, thirdPartyServiceRestTemplate);
    }

    @Bean
    public RestTemplate thirdPartyServiceRestTemplate(
            ThirdPartyServiceProperties properties,
            RestTemplateBuilder restTemplateBuilder
    ) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectionTimeoutMillis()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()))
                .build();
    }

    public static RetryTemplate createRetryTemplate(ThirdPartyServiceProperties properties) {
        var retryableExceptions = Map.<Class<? extends Throwable>, Boolean>of(
                HttpServerErrorException.class, TRUE
        );
        var retryPolicy = new SimpleRetryPolicy(properties.getRetryMax(), retryableExceptions);
        var backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(properties.getRetryBackoffMillis());
        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
