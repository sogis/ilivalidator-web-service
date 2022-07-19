package ch.so.agi.ilivalidator.controller;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.so.agi.ilivalidator.Utils;
import ch.so.agi.ilivalidator.model.JobResponse;
import ch.so.agi.ilivalidator.service.FilesystemStorageService;
import ch.so.agi.ilivalidator.service.IlivalidatorService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@ConditionalOnProperty(
        value="app.restApiEnabled", 
        havingValue = "true", 
        matchIfMissing = false)
@RestController
public class ApiController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static String LOG_ENDPOINT = "logs";
    
    @Autowired
    private FilesystemStorageService fileStorageService;

    @Autowired
    private JobScheduler jobScheduler;
    
    @Autowired
    private IlivalidatorService ilivalidatorService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;


    // TODO:
    // openapi?
    
    @PostMapping(value="/rest/jobs", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadFile(@RequestParam(name="file", required=true) @RequestBody MultipartFile file, 
            @RequestParam(name="allObjectsAccessible", required=false, defaultValue="true") String allObjectsAccessible, 
            @RequestParam(name="configFile", required=false, defaultValue="on") String configFile) {
        
        log.debug(allObjectsAccessible.toString());
        log.debug(configFile.toString());

        Path uploadedFile = fileStorageService.store(file);        
        log.debug(uploadedFile.toAbsolutePath().toString());
        
        String inputFileName = uploadedFile.toAbsolutePath().toString();
        String logFileName = Utils.getLogFileName(inputFileName);
        
        JobId jobId = jobScheduler.enqueue(() -> ilivalidatorService.validate(inputFileName, logFileName, allObjectsAccessible, configFile));
        log.debug(jobId.toString());

        return ResponseEntity
                .accepted()
                .header("Operation-Location", getHost()+"/rest/jobs/"+jobId)
                .body(null);        
    }
    
    @GetMapping("/rest/jobs/{jobId}")
    public ResponseEntity<?> getJobById(@PathVariable String jobId) {
        String stmt = """
SELECT
    id, jobAsJson, state, createdAt, updatedAt
FROM
    jobrunr_jobs
WHERE
    id =?             
""";        
        JobResponse jobResponse = jdbcTemplate.queryForObject(stmt, new RowMapper<JobResponse>() {
            @Override
            public JobResponse mapRow(ResultSet rs, int rowNum) throws SQLException {   
                String state = rs.getString("state");

                String logFileLocation = null;
                String xtfLogFileLocation = null;
                if (state.equalsIgnoreCase("SUCCEEDED")) {
                    try {
                        JsonNode response = objectMapper.readTree(rs.getString("jobAsJson"));                    
                        ArrayNode jobParameters = (ArrayNode) response.get("jobDetails").get("jobParameters");
                        String logFileName = jobParameters.get(1).get("object").asText();            
                        logFileLocation = Utils.fixUrl(getHost() + "/" + LOG_ENDPOINT + "/" + Utils.getLogFileUrlPathElement(logFileName));
                        xtfLogFileLocation = logFileLocation + ".xtf";
                    } catch (JsonProcessingException e) {
                        new RuntimeException(e.getMessage());
                    }
                }

                JobResponse jobResponse = null;
                    jobResponse = new JobResponse(
                            rs.getTimestamp("createdAt").toLocalDateTime(),
                            rs.getTimestamp("updatedAt").toLocalDateTime(),
                            state,
                            logFileLocation,
                            xtfLogFileLocation
                        );

                return jobResponse;
            }
        }, jobId);
        
        if (!jobResponse.status().equalsIgnoreCase("SUCCEEDED")) {
            return ResponseEntity.ok().header("Retry-After", "30").body(jobResponse);
        } else {
            return ResponseEntity.ok().body(jobResponse);
        }
    }
        
    private String getHost() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
