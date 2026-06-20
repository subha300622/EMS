package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public class ErrorResponse {
    @Schema(example = "true")
    private boolean success;
    @Schema(example = "string")
    private String message;
    @Schema(example = "EMP101")
    private String errorCode;
    @Schema(example = "string")
    private String timestamp;

    public ErrorResponse() {}

    public ErrorResponse(boolean success, String message, String errorCode, String timestamp) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = timestamp;
    }

    public static ErrorResponse error(String message, String errorCode) {
        return new ErrorResponse(false, message, errorCode, Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString());
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
