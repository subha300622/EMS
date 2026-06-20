package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class ChangeRequestResponse {

    @Schema(example = "1")
    private Long requestId;
    @Schema(example = "string")
    private String requestNumber;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String submittedAt;
    @Schema(example = "string")
    private String message;

    public ChangeRequestResponse() {}

    public ChangeRequestResponse(Long requestId, String requestNumber, String status, String submittedAt, String message) {
        this.requestId = requestId;
        this.requestNumber = requestNumber;
        this.status = status;
        this.submittedAt = submittedAt;
        this.message = message;
    }

    // Getters and Setters
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
