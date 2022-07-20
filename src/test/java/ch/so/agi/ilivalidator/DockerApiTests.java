package ch.so.agi.ilivalidator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Tag("docker")
@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DockerApiTests extends ApiTests {
    private static int exposedPort = 8080;
    
    @Autowired
    protected TestRestTemplate restTemplate2;

    @Container
    public static GenericContainer<?> ilivalidatorWebService = new GenericContainer<>("edigonzales/ilivalidator-web-service:latest")
            .waitingFor(Wait.forHttp("/actuator/health"))
            .withEnv("JDBC_URL", "jdbc:sqlite:./jobrunr_db.sqlite")
            .withEnv("DOC_BASE", "/tmp/")
            .withEnv("WORK_DIRECTORY", "/tmp/")
            .withExposedPorts(exposedPort)
            .withLogConsumer(new Slf4jLogConsumer(logger));

    @BeforeAll
    public void setup() {
        port = String.valueOf(ilivalidatorWebService.getMappedPort(exposedPort));
    }
}
