package com.example.ems.common.dto;

import java.time.Instant;

public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
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
