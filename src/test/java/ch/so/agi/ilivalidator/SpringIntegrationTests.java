package ch.so.agi.ilivalidator;


import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;

//https://github.com/spring-guides/gs-uploading-files/blob/master/complete/src/test/java/hello/FileUploadIntegrationTests.java
//https://github.com/cosmic-cowboy/spring-boot-multipart-file-upload/blob/master/spring-boot-multipart-file-upload/src/test/java/com/slgerkamp/introductory/spring/boot/multipart/file/upload/application/controller/MultipartControllerTest.java
//https://github.com/rest-assured/rest-assured

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SpringIntegrationTests extends IntegrationTests {
    @LocalServerPort
    int randomServerPort;
    
    @Before
    public void setup() {
    		RestAssured.port = randomServerPort;
    }
}
