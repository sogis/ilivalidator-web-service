package ch.so.agi.ilivalidator.service;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();

    void store(MultipartFile file) throws IOException;
    
    Path load(String filename);

    void delete(String filename);
}
