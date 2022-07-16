package ch.so.agi.ilivalidator.controller;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ch.so.agi.ilivalidator.service.FileStorageService;

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
    private FileStorageService fileStorageService;

    @PostMapping("/rest/upload")
    public ResponseEntity<?> uploadFile(@RequestParam(name="file", required=true) MultipartFile file, 
            @RequestParam(name="allObjectsAccessible", required=false, defaultValue="true") Boolean allObjectsAccessible, 
            @RequestParam(name="configFile", required=false, defaultValue="on") Boolean configFile) {
        
        log.debug(allObjectsAccessible.toString());
        log.debug(configFile.toString());

        Path uploadedFile = fileStorageService.storeFile(file);        
        log.debug(uploadedFile.toAbsolutePath().toString());

        return ResponseEntity.ok()
                .body("fubar");

        
//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/downloadFile/")
//                .path(fileName)
//                .toUriString();

//        return new UploadFileResponse(fileName, fileDownloadUri,
//                file.getContentType(), file.getSize());
    }

}
