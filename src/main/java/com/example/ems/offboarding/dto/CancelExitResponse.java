package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CancelExitResponse {

    @Schema(example = "1")
    private Long exitRequestId;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
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
