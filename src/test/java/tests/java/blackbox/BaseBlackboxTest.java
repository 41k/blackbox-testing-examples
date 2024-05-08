package tests.java.blackbox;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import root.ApplicationRunner;
import root.repository.ProcessedDataRepository;
import tests.java.blackbox.configuration.BlackboxTestConfiguration;
import tests.java.blackbox.steps.KafkaConsumerSteps;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"test"})
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes={ApplicationRunner.class, BlackboxTestConfiguration.class})
public class BaseBlackboxTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected CacheManager cacheManager;
    @Autowired
    protected ProcessedDataRepository processedDataRepository;
    @Autowired
    protected KafkaConsumerSteps dataProcessingOutputKafkaConsumer;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    @AfterEach
    public void cleanup() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        processedDataRepository.deleteAll();
        processedDataRepository.flush();
        dataProcessingOutputKafkaConsumer.cleanTopic();
    }
}
