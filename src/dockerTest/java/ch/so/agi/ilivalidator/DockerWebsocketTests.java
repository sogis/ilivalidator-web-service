package ch.so.agi.ilivalidator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
public class DockerWebsocketTests extends WebsocketTests {
    private static int exposedPort = 8080;
    
    @Container
    public static GenericContainer<?> ilivalidatorWebService = new GenericContainer<>("edigonzales/ilivalidator-web-service:latest")
            .waitingFor(Wait.forHttp("/actuator/health"))
            .withEnv("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"))
            .withEnv("AWS_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY")) 
            .withEnv("DOC_BASE", "/tmp/")
            .withEnv("WORK_DIRECTORY", "/tmp/")
            .withExposedPorts(exposedPort)
            .withLogConsumer(new Slf4jLogConsumer(logger));

    @BeforeAll
    public void setup() {
        port = String.valueOf(ilivalidatorWebService.getMappedPort(exposedPort));
    }
}
