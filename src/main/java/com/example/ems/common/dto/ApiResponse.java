package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public class ApiResponse<T> {
    @Schema(example = "true")
    private boolean success;
    @Schema(example = "string")
    private String message;
    @Schema(example = "string")
    private String timestamp;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, String timestamp, T data) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString(), data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString(), null);
    }

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
