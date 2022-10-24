package ch.so.agi.ilivalidator.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResponse(
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String status,
            String validationStatus,
            String logFileLocation,
            String xtfLogFileLocation
        ) {}
