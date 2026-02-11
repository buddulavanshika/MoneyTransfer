package com.mts.domain.dto;

import java.util.Map;

public class ErrorResponse {
    private final String code;
    private final String message;
    private final int status;
    private final String path;
    private final String timestamp;
    private final String correlationId;
    private final Map<String, Object> details;

    public ErrorResponse(String code, String message, int status, String path,
                         String timestamp, String correlationId, Map<String, Object> details) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
        this.details = details;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
    public String getPath() { return path; }
    public String getTimestamp() { return timestamp; }
    public String getCorrelationId() { return correlationId; }
    public Map<String, Object> getDetails() { return details; }
}