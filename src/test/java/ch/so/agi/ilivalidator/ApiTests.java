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
import java.util.concurrent.TimeUnit;

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
    private int RESULT_POLL_INTERVAL = 5; // seconds
    private int RESULT_WAIT = 5; // minutes

    @Test
    public void validation_Ok_ili1() throws Exception {
        String serverUrl = "http://localhost:"+port+REST_ENDPOINT;

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
        parameters.add("file", new FileSystemResource("src/test/data/ch_254900.itf"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        headers.set("Accept", "text/plain");

        // Datei hochladen und Response-Status-Code auswerten
        ResponseEntity<String> postResponse = restTemplate.postForEntity(
                serverUrl, new HttpEntity<MultiValueMap<String, Object>>(parameters, headers), String.class);

        assertEquals(202, postResponse.getStatusCode().value());
        
        // Warten, bis die Validierung durch ist (=SUCCEEDED)
        String operationLocation = postResponse.getHeaders().toSingleValueMap().get(OPERATION_LOCATION_HEADER);
        
        await()
            .with().pollInterval(RESULT_POLL_INTERVAL, TimeUnit.SECONDS)
            .and()
            .with().atMost(RESULT_WAIT, TimeUnit.MINUTES)
            .until(new MyCallable(operationLocation, restTemplate));

        // Logfile herunterladen und auswerten
        ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);
        System.out.println(jobResponse.getBody().logFileLocation());
        
        

    } 
    
    public class MyCallable implements Callable<Boolean> {  
        private final String operationLocation;
        private final TestRestTemplate restTemplate;
           
        public MyCallable(String operationLocation, TestRestTemplate restTemplate) {
            this.operationLocation = operationLocation;
            this.restTemplate = restTemplate;
        }
       
        @Override
        public Boolean call() throws Exception {
            ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);
            if (jobResponse.getBody().status().equalsIgnoreCase("SUCCEEDED")) {
                return true;
            } 
            return jobResponse.getBody().status().equalsIgnoreCase("SUCCEEDED") ? true : false;            
        }
    }

}
