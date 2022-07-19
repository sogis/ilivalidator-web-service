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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import ch.so.agi.ilivalidator.model.JobResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.*;

public abstract class ApiTests {
    static Logger logger = LoggerFactory.getLogger(ApiTests.class);

    @LocalServerPort
    protected String port;

    @Autowired
    protected TestRestTemplate restTemplate;

    private String REST_ENDPOINT = "/rest/jobs/";
    private String OPERATION_LOCATION_HEADER = "Operation-Location";
    //private String RETRY_AFTER_HEADER = "Retry-After";
    private int RESULT_POLL_DELAY = 5; // seconds
    private int RESULT_POLL_INTERVAL = 5; // seconds
    private int RESULT_WAIT = 5; // minutes

    @Test
    public void validationDoneInterlis1File() throws Exception {
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
        URL logFileUrl = new URL(jobResponse.getBody().logFileLocation());

        String logfileContents = null;
        try (InputStream in = logFileUrl.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            logfileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertTrue(logfileContents.contains("Info: ...validation done"));
    } 
    
    @Test
    public void validationFailedInterlis23FileWithConfigFileAndAllObjectsAccessible() throws Exception {
        String serverUrl = "http://localhost:"+port+REST_ENDPOINT;

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
        parameters.add("file", new FileSystemResource("src/test/data/2457_Messen_vorher.xtf"));

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
            .with().pollDelay(RESULT_POLL_DELAY, TimeUnit.SECONDS).pollInterval(RESULT_POLL_INTERVAL, TimeUnit.SECONDS)
            .and()
            .with().atMost(RESULT_WAIT, TimeUnit.MINUTES)
            .until(new MyCallable(operationLocation, restTemplate));

        // Logfile herunterladen und auswerten
        ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);        
        URL logFileUrl = new URL(jobResponse.getBody().logFileLocation());

        String logFileContents = null;
        try (InputStream in = logFileUrl.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            logFileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertTrue(logFileContents.contains("so_nutzungsplanung_20171118.toml"));
        assertTrue(logFileContents.contains("Warning: line 5: SO_Nutzungsplanung_20171118.Rechtsvorschriften.Dokument: tid d3c20374-f6c5-48f9-8e1e-232b87a9d80a: invalid format of INTERLIS.URI value <34-Messen/Entscheide/34-36_45-E.pdf> in attribute TextImWeb"));
        assertTrue(logFileContents.contains("Error: line 61: SO_Nutzungsplanung_20171118.Rechtsvorschriften.HinweisWeitereDokumente: tid 6: Association SO_Nutzungsplanung_20171118.Rechtsvorschriften.HinweisWeitereDokumente must not have an OID (6)"));
        assertTrue(logFileContents.contains("Error: line 412: SO_Nutzungsplanung_20171118.Nutzungsplanung.Grundnutzung: tid 2d285daf-a5ab-4106-a453-58eef2e921ab: duplicate coord at (2599932.281, 1216063.38, NaN)"));
        assertTrue(logFileContents.contains("Error: line 140: SO_Nutzungsplanung_20171118.Nutzungsplanung.Typ_Ueberlagernd_Flaeche: tid 0723a0c8-46e4-4e4f-aba4-c75e90bece14: Attributwert Verbindlichkeit ist nicht identisch zum Objektkatalog: 'orientierend' - '6110'"));
        assertTrue(logFileContents.contains("Dokument 'https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/24-Brunnenthal/Reglemente/024_BZR.pdf' wurde nicht gefunden"));
        // Irgendwie ist das identisch aber doch nicht. FIXME.
        //assertTrue(logfileContents.contains("Error: SO_Nutzungsplanung_20171118.Nutzungsplanung.Ueberlagernd_Flaeche: tid 7478a32c-45d6-4b3f-8507-0b9f4bd308bf/Geometrie[1]: Intersection coord1 (2600228,240, 1217472,518), tids 7478a32c-45d6-4b3f-8507-0b9f4bd308bf/Geometrie[1], 9b8a1966-1482-4b1f-b576-968f4246e80a/Geometrie[1]"));
        assertTrue(logFileContents.contains("Error: Set Constraint SO_Nutzungsplanung_20171118_Validierung_20211006.Nutzungsplanung_Validierung.v_Ueberlagernd_Flaeche.laermempfindlichkeitsAreaCheck is not true"));
        assertTrue(logFileContents.contains("Info: validate set constraint SO_Nutzungsplanung_20171118_Validierung_20211006.Rechtsvorschriften_Validierung.v_HinweisWeitereDokumente.isValidDocumentsCycle..."));
        assertTrue(logFileContents.contains("Error: line 61: SO_Nutzungsplanung_20171118.Rechtsvorschriften.HinweisWeitereDokumente: tid 6: self loop found: 95efebb8-24df-4462-9af1-15500e341f04"));       
        assertTrue(logFileContents.contains("Info: ...validation failed"));
    }
    
    @Test
    public void validationFailedInterlis23FileWithoutConfigFileAndNotAllObjectsAccessible() throws Exception {
        String serverUrl = "http://localhost:"+port+REST_ENDPOINT;

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
        parameters.add("file", new FileSystemResource("src/test/data/2457_Messen_vorher.xtf"));
        parameters.add("configFile", "off");
        parameters.add("allObjectsAccessible", "false");

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
            .with().pollDelay(RESULT_POLL_DELAY, TimeUnit.SECONDS).pollInterval(RESULT_POLL_INTERVAL, TimeUnit.SECONDS)
            .and()
            .with().atMost(RESULT_WAIT, TimeUnit.MINUTES)
            .until(new MyCallable(operationLocation, restTemplate));

        // Logfile herunterladen und auswerten
        ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);        
        URL logFileUrl = new URL(jobResponse.getBody().logFileLocation());

        String logFileContents = null;
        try (InputStream in = logFileUrl.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            logFileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertFalse(logFileContents.contains("configFile"));
        assertTrue(logFileContents.contains("assume unknown external objects"));
        
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
            logger.info("*******************************************************");
            logger.info("polling: {}", operationLocation);
            logger.info("*******************************************************");
            ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);
            return jobResponse.getBody().status().equalsIgnoreCase("SUCCEEDED") ? true : false;            
        }
    }

}
