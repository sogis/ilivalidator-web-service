package ch.so.agi.ilivalidator.controller;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
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

import ch.so.agi.ilivalidator.model.JobResponse;
import ch.so.agi.ilivalidator.service.FilesystemStorageService;
import ch.so.agi.ilivalidator.service.IlivalidatorJobService;

@ConditionalOnProperty(
        value="app.restApiEnabled", 
        havingValue = "true", 
        matchIfMissing = false)
@RestController
public class ApiController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    @Value("${app.workDirectory}")
//    private String workDirectory;
//    
//    @Value("${app.folderPrefix}")
//    private String folderPrefix;
    
    @Autowired
    private FilesystemStorageService fileStorageService;

    @Autowired
    private JobScheduler jobScheduler;
    
    @Autowired
    private IlivalidatorJobService ilivalidatorService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    
    // TODO:
    // /rest/jobs als Endpunkt?
    
    @PostMapping("/rest/upload")
    public ResponseEntity<?> uploadFile(@RequestParam(name="file", required=true) MultipartFile file, 
            @RequestParam(name="allObjectsAccessible", required=false, defaultValue="true") Boolean allObjectsAccessible, 
            @RequestParam(name="configFile", required=false, defaultValue="on") Boolean configFile) {
        
        log.debug(allObjectsAccessible.toString());
        log.debug(configFile.toString());

        Path uploadedFile = fileStorageService.store(file);        
        log.info(uploadedFile.toAbsolutePath().toString());
        
        String inputFilename = uploadedFile.toAbsolutePath().toString();
        JobId jobId = jobScheduler.enqueue(() -> ilivalidatorService.validate(inputFilename));
        log.info(jobId.toString());

        return ResponseEntity
                .accepted()
                .header("Operation-Location", getHost()+"/rest/operations/"+jobId)
                .body(null);
        

        
//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/downloadFile/")
//                .path(fileName)
//                .toUriString();

//        return new UploadFileResponse(fileName, fileDownloadUri,
//                file.getContentType(), file.getSize());
    }
    
    @GetMapping("/rest/operations/{jobId}")
    public ResponseEntity<?> getJobById(@PathVariable String jobId) {
        String stmt = """
SELECT
    id, state, createdAt, updatedAt
FROM
    jobrunr_jobs
WHERE
    id =?             
""";
        
        // TODO: 
        // - record ohne null in json
        // - mehrere constructors für record.
        
        JobResponse jobResponse = jdbcTemplate.queryForObject(stmt, new RowMapper<JobResponse>() {
            @Override
            public JobResponse mapRow(ResultSet rs, int rowNum) throws SQLException {                
                JobResponse jobResponse = new JobResponse(
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getTimestamp("updatedAt").toLocalDateTime(),
                        rs.getString("state"),
                        null,
                        null
                    );

                return jobResponse;
            }
        }, jobId);
        
        log.info(jobResponse.toString());
        
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
