package blackbox

import org.springframework.beans.factory.annotation.Autowired
import root.service.ThirdPartyServiceClient

import static TestConstants.JSON_CONTENT_TYPE
import static com.github.tomakehurst.wiremock.client.WireMock.*

class ThirdPartyServiceClientCacheTest extends BaseBlackboxTest {

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient

    def 'should cache 3rd party service responses'() {
        given:
        def id1 = 'id-1'
        def url1 = "/additional-data/$id1"
        def data1 = "data-for-$id1"
        mockCallToThirdPartyService(url1, data1)

        and:
        def id2 = 'id-2'
        def url2 = "/additional-data/$id2"
        def data2 = "data-for-$id2"
        mockCallToThirdPartyService(url2, data2)

        and:
        def id3 = 'id-3'
        def url3 = "/additional-data/$id3"
        def data3 = "data-for-$id3"
        mockCallToThirdPartyService(url3, data3)

        expect:
        thirdPartyServiceClient.getAdditionalData(id1) == data1
        thirdPartyServiceClient.getAdditionalData(id1) == data1

        and:
        thirdPartyServiceClient.getAdditionalData(id2) == data2
        thirdPartyServiceClient.getAdditionalData(id2) == data2

        and:
        thirdPartyServiceClient.getAdditionalData(id3) == data3
        thirdPartyServiceClient.getAdditionalData(id3) == data3

        and: '3rd party responses should be cached per id'
        verify(exactly(1), getRequestedFor(urlEqualTo(url1)))
        verify(exactly(1), getRequestedFor(urlEqualTo(url2)))
        verify(exactly(1), getRequestedFor(urlEqualTo(url3)))
    }

    private static void mockCallToThirdPartyService(String url, String data) {
        stubFor(get(urlPathEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""  {"additionalData":"$data"}  """)
                        .withHeader('Content-Type', JSON_CONTENT_TYPE)
                        .withHeader('Connection', 'close')))
    }
}
