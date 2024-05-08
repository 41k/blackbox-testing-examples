package tests.groovy.blackbox

import io.restassured.RestAssured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cache.CacheManager
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import root.ApplicationRunner
import root.repository.ProcessedDataRepository
import spock.lang.Specification
import tests.groovy.blackbox.configuration.BlackboxTestConfiguration
import tests.groovy.blackbox.steps.KafkaConsumerSteps

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(['test'])
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes=[ApplicationRunner, BlackboxTestConfiguration])
abstract class BaseBlackboxTest extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    protected CacheManager cacheManager
    @Autowired
    protected ProcessedDataRepository processedDataRepository
    @Autowired
    protected KafkaConsumerSteps dataProcessingOutputKafkaConsumer

    def setup() {
        RestAssured.port = port
    }

    def cleanup() {
        cacheManager.cacheNames.each { cacheName -> cacheManager.getCache(cacheName).clear() }
        processedDataRepository.deleteAll()
        processedDataRepository.flush()
        dataProcessingOutputKafkaConsumer.cleanTopic()
    }
}
