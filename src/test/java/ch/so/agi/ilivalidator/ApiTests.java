package ch.so.agi.ilivalidator;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import ch.so.agi.ilivalidator.model.JobResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.*;

// TODO: make abstract etc. etc. 

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiTests {
    static Logger logger = LoggerFactory.getLogger(ApiTests.class);

    @LocalServerPort
    protected String port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String REST_ENDPOINT = "/rest/jobs/";
    private String OPERATION_LOCATION_HEADER = "Operation-Location";
    private String RETRY_AFTER_HEADER = "Retry-After";

    @Test
    public void validation_Ok_ili1() throws Exception {
        final String serverUrl = "http://localhost:"+port+REST_ENDPOINT;
        System.out.println(serverUrl);

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
        parameters.add("file", new FileSystemResource("src/test/data/ch_254900.itf"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        headers.set("Accept", "text/plain");

        ResponseEntity<String> postResponse = restTemplate.postForEntity(
                serverUrl, new HttpEntity<MultiValueMap<String, Object>>(parameters, headers), String.class);

        assertEquals(202, postResponse.getStatusCode().value());
        
        
        
        
        
        String operationLocation = postResponse.getHeaders().toSingleValueMap().get(OPERATION_LOCATION_HEADER);
        
        Thread.sleep(10000);
        await().until(newUserIsAdded(operationLocation));

        
        
        
//        assertEquals(200, jobResponse.getStatusCode().value());
//
//        System.out.println(jobResponse.getStatusCode());
//        System.out.println(jobResponse.getBody());
//        System.out.println(jobResponse.getHeaders().toSingleValueMap());

        System.out.println(postResponse.getStatusCode());
        System.out.println(postResponse.getBody());
        System.out.println(postResponse.getHeaders().toSingleValueMap().get(OPERATION_LOCATION_HEADER));

    } 

    
    
    private Callable<Boolean> newUserIsAdded(String operationLocation) {
        ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);
        System.out.println("****************"+operationLocation);
        System.out.println("****************"+jobResponse.getBody().status());
   
        return () -> jobResponse.getBody().status().length() > 10;
    }
}
