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
import io.restassured.response.Response;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
	
	//@Test
	/*
	 * INTERLIS transfer file contains some errors but are ignored
	 * or reduced to warnings by config file. Hence the validation
	 * must be successfull.
	 */
	public void successfulValidationTestWithConfigFile() {
		File file = new File("src/test/data/ch_254900_error.itf");
		
		given().
			param("configFile", "on").
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("...validation done")).
			body(containsString("Info: configFile"));
	}

	@Test
	/*
	 * There is no according config file to the INTERLIS
	 * transfer/model file. Nonetheless the validation is done.
	 * We have to use another model, if there will be a config
	 * file for this one.
	 */
	public void validationTestWithoutConfigFile() {
		File file = new File("src/test/data/roh_20170717A.xtf");
				
		Response response =
				given().
					multiPart("file", file).
				when().
					post("/ilivalidator/").
				then().extract()
			.response();
		
		// http://www.vogella.com/tutorials/Hamcrest/article.html
        assertThat(response.asString().indexOf("Info: configFile"), is(-1));

		
	}

}
