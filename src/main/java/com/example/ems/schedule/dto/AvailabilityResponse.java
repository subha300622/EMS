package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AvailabilityResponse {

    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String updatedAt;
    @Schema(example = "string")
    private String message;

    public AvailabilityResponse() {}

    public AvailabilityResponse(String status, String updatedAt, String message) {
        this.status = status;
        this.updatedAt = updatedAt;
        this.message = message;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
