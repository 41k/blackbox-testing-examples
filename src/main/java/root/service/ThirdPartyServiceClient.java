package root.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import root.dto.ThirdPartyServiceResponse;

import java.net.URI;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
public class ThirdPartyServiceClient {

    private final String url;
    private final RetryTemplate retryTemplate;
    private final RestTemplate restTemplate;

    @Cacheable(value = "additionalData")
    public String getAdditionalData(String id) {
        var uri = URI.create(url + id);
        var response = retryTemplate.execute(retryContext ->
                restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse.class));
        return Optional.ofNullable(response.getBody())
                .map(ThirdPartyServiceResponse::getAdditionalData)
                .orElse(EMPTY);
    }
}
