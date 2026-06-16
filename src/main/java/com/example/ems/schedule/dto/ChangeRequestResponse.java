package com.example.ems.schedule.dto;

public class ChangeRequestResponse {

    private Long requestId;
    private String requestNumber;
    private String status;
    private String submittedAt;
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
