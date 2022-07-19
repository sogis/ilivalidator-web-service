package ch.so.agi.ilivalidator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ch.so.agi.ilivalidator.service.FilesystemStorageService;
import io.swagger.v3.oas.annotations.Hidden;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Hidden
@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.workDirectory}")
    private String workDirectory;
    
    @Value("${app.folderPrefix}")
    private String folderPrefix;
    
    @GetMapping("/ping")
    public ResponseEntity<?> ping()  {
        return new ResponseEntity<String>("ilivalidator-web-service", HttpStatus.OK);
    }
    
    @GetMapping("/logs/{key}/{filename}") 
    public ResponseEntity<?> getLog(@PathVariable String key, @PathVariable String filename) {        
        MediaType mediaType = new MediaType("text", "plain", StandardCharsets.UTF_8);
        if (filename.endsWith(".xtf")) {
            mediaType = MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE);
        }
        
        try {
            File logFile = Paths.get(workDirectory, key, filename).toFile();
            InputStream is = new FileInputStream(logFile);

            return ResponseEntity.ok().header("Content-Type", "charset=utf-8")
                    .contentLength(logFile.length())
                    .contentType(mediaType)
                    .body(new InputStreamResource(is));

        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);  
        }
    }
  
    /*
     * Verzeichnisse löschen, die älter als 60x60 Sekunden alt sind.
     */
    @Scheduled(cron="0 0/2 * * * ?")
    //@Scheduled(cron="0 * * * * *")
    private void cleanUp() {    
        java.io.File[] tmpDirs = new java.io.File(workDirectory).listFiles();
        if(tmpDirs!=null) {
            for (java.io.File tmpDir : tmpDirs) {
                if (tmpDir.getName().startsWith(folderPrefix)) {
                    try {
                        FileTime creationTime = (FileTime) Files.getAttribute(Paths.get(tmpDir.getAbsolutePath()), "creationTime");                    
                        Instant now = Instant.now();
                        
                        long fileAge = now.getEpochSecond() - creationTime.toInstant().getEpochSecond();
                        if (fileAge > 60*60) {
                            log.info("deleting {}", tmpDir.getAbsolutePath());
                            FileSystemUtils.deleteRecursively(tmpDir);
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
}
