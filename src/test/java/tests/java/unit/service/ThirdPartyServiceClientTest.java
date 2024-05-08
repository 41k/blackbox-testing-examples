package tests.java.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import root.configuration.ApplicationConfiguration;
import root.configuration.properties.ThirdPartyServiceProperties;
import root.dto.ThirdPartyServiceResponse;
import root.service.ThirdPartyServiceClient;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static tests.java.TestConstants.ADDITIONAL_DATA;
import static tests.java.TestConstants.ADDITIONAL_DATA_ID;

@ExtendWith(MockitoExtension.class)
public class ThirdPartyServiceClientTest {

    private static final String THIRD_PARTY_SERVICE_URL = "http://third-party-service.com/additional-data/";
    private static final URI ADDITIONAL_DATA_URI = URI.create(THIRD_PARTY_SERVICE_URL + ADDITIONAL_DATA_ID);
    private static final ResponseEntity<ThirdPartyServiceResponse> THIRD_PARTY_SERVICE_RESPONSE =
            new ResponseEntity<>(new ThirdPartyServiceResponse(ADDITIONAL_DATA), OK);
    private static final int N_RETRIES = 3;

    @Mock
    private RestTemplate restTemplate;

    private ThirdPartyServiceClient thirdPartyServiceClient;

    @BeforeEach
    void setUp() {
        var thirdPartyServiceProperties = new ThirdPartyServiceProperties();
        thirdPartyServiceProperties.setRetryMax(N_RETRIES);
        thirdPartyServiceProperties.setRetryBackoffMillis(50);
        var retryTemplate = ApplicationConfiguration.createRetryTemplate(thirdPartyServiceProperties);
        thirdPartyServiceClient = new ThirdPartyServiceClient(THIRD_PARTY_SERVICE_URL, retryTemplate, restTemplate);
    }

    @Test
    void shouldRetrieveAdditionalDataSuccessfullyWithRetries() {
        // GIVEN:
        when(restTemplate.exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse.class))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR))
                .thenReturn(THIRD_PARTY_SERVICE_RESPONSE);

        // WHEN:
        var additionalData = thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID);

        // THEN:
        assertThat(additionalData).isEqualTo(ADDITIONAL_DATA);

        // AND:
        verify(restTemplate, times(N_RETRIES)).exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse.class);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void shouldReThrowExceptionWhenRetriesAreExhausted() {
        // GIVEN:
        when(restTemplate.exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse.class))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        // EXPECT:
        assertThatThrownBy(() -> thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID))
                .isInstanceOf(HttpServerErrorException.class)
                .hasMessage("500 INTERNAL_SERVER_ERROR");

        // AND:
        verify(restTemplate, times(N_RETRIES)).exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse.class);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void shouldReThrowClientExceptionWithoutRetries() {
        // GIVEN:
        when(restTemplate.exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse.class))
                .thenThrow(new HttpClientErrorException(BAD_REQUEST));

        // EXPECT:
        assertThatThrownBy(() -> thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 BAD_REQUEST");

        // AND:
        verify(restTemplate).exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse.class);
        verifyNoMoreInteractions(restTemplate);
    }
}
