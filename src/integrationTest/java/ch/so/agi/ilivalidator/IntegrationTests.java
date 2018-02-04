package ch.so.agi.ilivalidator;

import static io.restassured.RestAssured.given;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class IntegrationTests {
    @BeforeClass
    public static void setup() {
    		String port = System.getProperty("server.port");
    		if (port == null) {
    			RestAssured.port = Integer.valueOf(8888);
    		} else {
    			RestAssured.port = Integer.valueOf(port);
    		}

//    		String basePath = System.getProperty("server.base");
//    		if(basePath == null) {
//    			basePath = "/ilivalidator/";
//    		}
//    		RestAssured.basePath = basePath;

    		String baseHost = System.getProperty("server.host");
    		if(baseHost == null) {
    			baseHost = "http://localhost";
    		}
    		RestAssured.baseURI = baseHost;

    }

	
	@Test
	public void makeSureThatGoogleIsUp() {
		
		System.out.println(RestAssured.baseURI);
		
		RestAssured.port = 80;
		given().when().get("http://www.google.com").then().statusCode(200);
	}
}
