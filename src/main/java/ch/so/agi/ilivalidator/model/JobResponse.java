package ch.so.agi.ilivalidator.model;

import java.time.LocalDateTime;
import java.util.Date;

public record JobResponse(
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String status,
            String logFileLocation,
            String xtfLogFileLocation
        ) {}
