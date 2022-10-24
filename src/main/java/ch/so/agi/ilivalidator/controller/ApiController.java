package ch.so.agi.ilivalidator.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import ch.so.agi.ilivalidator.Utils;
import ch.so.agi.ilivalidator.model.JobResponse;
import ch.so.agi.ilivalidator.service.FilesystemStorageService;
import ch.so.agi.ilivalidator.service.IlivalidatorService;
import ch.so.agi.ilivalidator.service.ValidationResult;
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
    StorageProvider storageProvider;

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
        
        Job job = storageProvider.getJobById(UUID.fromString(jobId));        
        String logFileName = job.getJobDetails().getJobParameters().get(1).getObject().toString();            

        String logFileLocation = null;
        String xtfLogFileLocation = null;
        String validationResult = null;
        if (job.getJobState().getName().equals(StateName.SUCCEEDED)) {
            logFileLocation = Utils.fixUrl(getHost() + "/" + LOG_ENDPOINT + "/" + Utils.getLogFileUrlPathElement(logFileName));
            xtfLogFileLocation = logFileLocation + ".xtf"; 
            
            try {
                // JobResult nur bei Pro-Version
                String content = Files.readString(Paths.get(logFileName));
                if (content.contains("...validation done")) {
                    validationResult = ValidationResult.SUCCEEDED.toString();
                } else if (content.contains("...validation failed")) {
                    validationResult = ValidationResult.FAILED.toString();
                } else {
                    validationResult = ValidationResult.UNKNOWN.toString();
                }
            } catch (IOException e) {
                validationResult = ValidationResult.UNKNOWN.toString();
                e.printStackTrace();
            }
        }
        
      JobResponse jobResponse = new JobResponse(
              LocalDateTime.ofInstant(job.getCreatedAt(), ZoneId.systemDefault()),
              LocalDateTime.ofInstant(job.getUpdatedAt(), ZoneId.systemDefault()),
              job.getState().name(),
              validationResult,
              logFileLocation,
              xtfLogFileLocation
          );
        
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
