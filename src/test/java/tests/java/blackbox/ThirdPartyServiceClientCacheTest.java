package tests.java.blackbox;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import root.service.ThirdPartyServiceClient;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static tests.java.TestConstants.THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL;

public class ThirdPartyServiceClientCacheTest extends BaseBlackboxTest {

    private static final String ADDITIONAL_DATA_PREFIX = "data-for-";

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    @Test
    @SneakyThrows
    void shouldCache3rdPartyServiceResponses() {
        // GIVEN:
        var id1 = "id-1";
        var uri1 = THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL + id1;
        var data1 = ADDITIONAL_DATA_PREFIX + id1;
        mockCallToThirdPartyService(uri1, data1);

        var id2 = "id-2";
        var uri2 = THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL + id2;
        var data2 = ADDITIONAL_DATA_PREFIX + id2;
        mockCallToThirdPartyService(uri2, data2);

        var id3 = "id-3";
        var uri3 = THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL + id3;
        var data3 = ADDITIONAL_DATA_PREFIX + id3;
        mockCallToThirdPartyService(uri3, data3);

        // EXPECT:
        // request to 3rd party service
        assertThat(thirdPartyServiceClient.getAdditionalData(id1)).isEqualTo(data1);
        // no request to 3rd party service - response is taken from cache
        assertThat(thirdPartyServiceClient.getAdditionalData(id1)).isEqualTo(data1);

        // request to 3rd party service
        assertThat(thirdPartyServiceClient.getAdditionalData(id2)).isEqualTo(data2);
        // no request to 3rd party service - response is taken from cache
        assertThat(thirdPartyServiceClient.getAdditionalData(id2)).isEqualTo(data2);

        // request to 3rd party service
        assertThat(thirdPartyServiceClient.getAdditionalData(id3)).isEqualTo(data3);
        // no request to 3rd party service - response is taken from cache
        assertThat(thirdPartyServiceClient.getAdditionalData(id3)).isEqualTo(data3);

        // wait for cache expiration
        TimeUnit.SECONDS.sleep(2);

        // request to 3rd party service
        assertThat(thirdPartyServiceClient.getAdditionalData(id1)).isEqualTo(data1);
        // request to 3rd party service
        assertThat(thirdPartyServiceClient.getAdditionalData(id2)).isEqualTo(data2);
        // request to 3rd party service
        assertThat(thirdPartyServiceClient.getAdditionalData(id3)).isEqualTo(data3);

        // verify that there were 2 calls to 3rd party service per each id
        verify(exactly(2), getRequestedFor(urlEqualTo(uri1)));
        verify(exactly(2), getRequestedFor(urlEqualTo(uri2)));
        verify(exactly(2), getRequestedFor(urlEqualTo(uri3)));
    }

    private void mockCallToThirdPartyService(String url, String data) {
        stubFor(get(urlPathEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""
                                    {
                                        "additionalData":"%s"
                                    }
                                """.formatted(data))));
    }
}
