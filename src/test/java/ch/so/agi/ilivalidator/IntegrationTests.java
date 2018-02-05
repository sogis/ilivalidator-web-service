package ch.so.agi.ilivalidator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.restassured.response.Response;

/*
 * This class can be inherited by other classes to run 
 * integration tests:
 * - Spring Boot
 * - Docker Image
 */
public abstract class IntegrationTests {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/*
	 * Simple index.html test.
	 */
	@Test
	public void indexPageTest() {				
		given().
		when().
        	get("/ilivalidator/").
        then().
        	statusCode(200).
        	body("html.head.title", equalTo("ilivalidator web service"));
	}
	
	/*
	 * We push the upload button but without sending a file
	 * to validate. It should redirect to the starting page.
	 * Not sure about how to implement this in rest assured:
	 * Now I create an empty file and checking the file size
	 * in the relevant if-clause. Testing the body seems
	 * not work though...
	 */
	@Test
	public void noFileUploadTest() throws IOException {
		File file = tempFolder.newFile("tempFile.txt");

		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
	    	statusCode(302);//.
	    	//body("html.head.title", equalTo("ilivalidator web service")).log().all();
	}	
	
	/*
	 * Upload a text file with nonsense content and
	 * provoke a iox exception.
	 */
	@Test
	public void uploadNonsenseFileTest() throws IOException {
		File file = new File("src/test/data/nonsense.txt");

		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
	    	statusCode(400).
	    	body(containsString("could not parse file: nonsense.txt"));
	}	
	
	@Test
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
	
	@Test
	public void unsuccessfulValidationTest() {
		File file = new File("src/test/data/ch_254900_error.itf");
		
		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200);//.
			//body(containsString("...validation failed"));
	}
	
	/*
	 * INTERLIS transfer file contains some errors but these are ignored
	 * or reduced to warnings by configuration file. Hence the validation
	 * must be successful.
	 */
	@Test
	public void successfulValidationTestWithConfigFile() {
		File file = new File("src/test/data/roh_20170717A_errors.xtf");
		
		given().
			param("configFile", "on").
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("Warning: line")).
			body(containsString("...validation done")).
			body(containsString("Info: configFile"));
	}

	/*
	 * There is no according configuration file to the INTERLIS
	 * transfer/model file. Nonetheless the validation is done.
	 * "Info: configFile" should not be in the output even
	 * with 'configFile' = 'on'.
	 */
	@Test
	public void successfulValidationTestWithoutConfigFile() {
		File file = new File("src/test/data/agglo_20170529.xtf");
				
		Response response =
				given().
					param("configFile", "on").
					multiPart("file", file).
				when().
					post("/ilivalidator/").
				then().
					statusCode(200).
				and().
					body(containsString("...validation done")).
				and().
					extract()
				.response();
		
		// assertThat: http://www.vogella.com/tutorials/Hamcrest/article.html
        assertThat(response.asString().indexOf("Info: configFile"), is(-1));		
	}
	

	/*
	 * The additional constraints are defined in a separate 
	 * model file. There must be also a configuration file that
	 * makes ilivalidator aware of the additional model.
	 */
	// !!!!! Does it test anything (im materiellen Sinne)?
	@Ignore("Validation INTERLIS model file not yet available.")
	@Test	
	public void additionalConstraintsValidationTest() {
		File file = new File("src/test/data/roh_20170717A_errors.xtf");
		
		given().
			param("configFile", "on").
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("Info: configFile")).			
			body(containsString("...validation failed"));
	}

}
