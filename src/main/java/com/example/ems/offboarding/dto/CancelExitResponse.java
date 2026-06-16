package com.example.ems.offboarding.dto;

public class CancelExitResponse {

    private Long exitRequestId;
    private String status;
    private String message;

    public CancelExitResponse() {}

    public CancelExitResponse(Long exitRequestId, String status, String message) {
        this.exitRequestId = exitRequestId;
        this.status = status;
        this.message = message;
    }

    public Long getExitRequestId() { return exitRequestId; }
    public void setExitRequestId(Long exitRequestId) { this.exitRequestId = exitRequestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
