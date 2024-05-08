package tests.groovy.unit.service

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import root.configuration.ApplicationConfiguration
import root.configuration.properties.ThirdPartyServiceProperties
import root.dto.ThirdPartyServiceResponse
import root.service.ThirdPartyServiceClient
import spock.lang.Specification

import static org.springframework.http.HttpStatus.*
import static tests.groovy.TestConstants.ADDITIONAL_DATA
import static tests.groovy.TestConstants.ADDITIONAL_DATA_ID

class ThirdPartyServiceClientTest extends Specification {

    private static final THIRD_PARTY_SERVICE_URL = 'http://third-party-service.com/additional-data/'
    private static final ADDITIONAL_DATA_URI = URI.create(THIRD_PARTY_SERVICE_URL + ADDITIONAL_DATA_ID)
    private static final THIRD_PARTY_SERVICE_RESPONSE =
            new ResponseEntity(new ThirdPartyServiceResponse(ADDITIONAL_DATA), OK)
    private static final N_RETRIES = 3;

    private thirdPartyServiceProperties = new ThirdPartyServiceProperties(retryMax: N_RETRIES, retryBackoffMillis: 50)
    private retryTemplate = ApplicationConfiguration.createRetryTemplate(thirdPartyServiceProperties)
    private restTemplate = Mock(RestTemplate)

    private thirdPartyServiceClient = new ThirdPartyServiceClient(THIRD_PARTY_SERVICE_URL, retryTemplate, restTemplate)

    def 'should retrieve additional data successfully with retries'() {
        when:
        def additionalData = thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID)

        then:
        N_RETRIES * restTemplate.exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse) >>
                { throw new HttpServerErrorException(INTERNAL_SERVER_ERROR) } >>
                { throw new HttpServerErrorException(INTERNAL_SERVER_ERROR) } >>
                THIRD_PARTY_SERVICE_RESPONSE
        0 * _

        and:
        additionalData == ADDITIONAL_DATA
    }

    def 'should re-throw exception when retries are exhausted'() {
        when:
        thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID)

        then:
        N_RETRIES * restTemplate.exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse) >> {
            throw new HttpServerErrorException(INTERNAL_SERVER_ERROR)
        }
        0 * _

        and:
        def exception = thrown(HttpServerErrorException)
        exception.message == '500 INTERNAL_SERVER_ERROR'
    }

    def 'should re-throw client exception without retries'() {
        when:
        thirdPartyServiceClient.getAdditionalData(ADDITIONAL_DATA_ID)

        then:
        1 * restTemplate.exchange(ADDITIONAL_DATA_URI, HttpMethod.GET, HttpEntity.EMPTY, ThirdPartyServiceResponse) >> {
            throw new HttpClientErrorException(BAD_REQUEST)
        }
        0 * _

        and:
        def exception = thrown(HttpClientErrorException)
        exception.message == '400 BAD_REQUEST'
    }
}
