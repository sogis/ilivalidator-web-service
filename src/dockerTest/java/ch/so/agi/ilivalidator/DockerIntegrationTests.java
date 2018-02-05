package ch.so.agi.ilivalidator;

import ch.so.agi.ilivalidator.IntegrationTests;

import org.junit.Before;

import io.restassured.RestAssured;

public class DockerIntegrationTests extends IntegrationTests {

	@Before
    public void setup() {
    		String port = System.getProperty("server.port");
    		if (port == null) {
    			// TODO: make port configurable 
    			RestAssured.port = Integer.valueOf(8888);
    		} else {
    			RestAssured.port = Integer.valueOf(port);
    		}
    		
    		String baseHost = System.getProperty("server.host");
    		if(baseHost == null) {
    			baseHost = "http://localhost";
    		}
    		RestAssured.baseURI = baseHost;

    }
}
