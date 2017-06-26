package ch.so.agi.ilivalidator;

import static io.restassured.RestAssured.given;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;

//https://github.com/spring-guides/gs-uploading-files/blob/master/complete/src/test/java/hello/FileUploadIntegrationTests.java
//https://github.com/cosmic-cowboy/spring-boot-multipart-file-upload/blob/master/spring-boot-multipart-file-upload/src/test/java/com/slgerkamp/introductory/spring/boot/multipart/file/upload/application/controller/MultipartControllerTest.java
//https://github.com/rest-assured/rest-assured

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class IntegrationTests {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @LocalServerPort
    int randomServerPort;
    
    @Before
    public void setPort() {
    	RestAssured.port = randomServerPort;
    }
	
	//@Test
	public void indexPageTest() {				
		given().
		when().
        	get("/ilivalidator/").
        then().
        	statusCode(200).
        	body("html.head.title", equalTo("ilivalidator web service"));
	}

	//@Test
	public void successfulValidationTest() {
		File file = new File("src/test/data/ch_254900.itf");
		
		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("...validation done"));
	}
	
	//@Test
	public void unsuccessfulValidationTest() {
		File file = new File("src/test/data/ch_254900_error.itf");
		
		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("...validation failed"));
	}
	
	@Test
	public void successfulValidationTestWithConfigFile() {
		File file = new File("src/test/data/ch_254900_error.itf");
		
		given().
			param("configFile", "on").
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("...validation done"));
	}

	//TODO
	public void validationTestWithConfigFileNotFound() {
		
	}

}
