package ch.so.agi.ilivalidator.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

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
}
