package tests.groovy.blackbox

import org.springframework.beans.factory.annotation.Autowired
import root.service.ThirdPartyServiceClient

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.apache.http.HttpStatus.SC_OK
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static tests.groovy.TestConstants.THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL

class ThirdPartyServiceClientCacheTest extends BaseBlackboxTest {

    private static final ADDITIONAL_DATA_PREFIX = 'data-for-'

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient

    def 'should cache 3rd party service responses'() {
        given:
        def id1 = 'id-1'
        def uri1 = THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL + id1
        def data1 = ADDITIONAL_DATA_PREFIX + id1
        mockCallToThirdPartyService(uri1, data1)

        and:
        def id2 = 'id-2'
        def uri2 = THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL + id2
        def data2 = ADDITIONAL_DATA_PREFIX + id2
        mockCallToThirdPartyService(uri2, data2)

        and:
        def id3 = 'id-3'
        def uri3 = THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL + id3
        def data3 = ADDITIONAL_DATA_PREFIX + id3
        mockCallToThirdPartyService(uri3, data3)

        expect: 'request to 3rd party service'
        thirdPartyServiceClient.getAdditionalData(id1) == data1
        and: 'no request to 3rd party service - response is taken from cache'
        thirdPartyServiceClient.getAdditionalData(id1) == data1

        and: 'request to 3rd party service'
        thirdPartyServiceClient.getAdditionalData(id2) == data2
        and: 'no request to 3rd party service - response is taken from cache'
        thirdPartyServiceClient.getAdditionalData(id2) == data2

        and: 'request to 3rd party service'
        thirdPartyServiceClient.getAdditionalData(id3) == data3
        and: 'no request to 3rd party service - response is taken from cache'
        thirdPartyServiceClient.getAdditionalData(id3) == data3

        and: 'wait for cache expiration'
        sleep(2000L)

        and: 'request to 3rd party service'
        thirdPartyServiceClient.getAdditionalData(id1) == data1
        and: 'request to 3rd party service'
        thirdPartyServiceClient.getAdditionalData(id2) == data2
        and: 'request to 3rd party service'
        thirdPartyServiceClient.getAdditionalData(id3) == data3

        and: 'there were 2 calls to 3rd party service per each id'
        verify(exactly(2), getRequestedFor(urlEqualTo(uri1)))
        verify(exactly(2), getRequestedFor(urlEqualTo(uri2)))
        verify(exactly(2), getRequestedFor(urlEqualTo(uri3)))
    }

    private void mockCallToThirdPartyService(String uri, String data) {
        stubFor(get(urlPathEqualTo(uri))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""  {"additionalData":"$data"}  """)))
    }
}
