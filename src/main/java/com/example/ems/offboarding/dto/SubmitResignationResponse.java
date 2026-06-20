package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class SubmitResignationResponse {

    @Schema(example = "string")
    private String message;
    @Schema(example = "1")
    private Long exitRequestId;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime submittedAt;

    public SubmitResignationResponse() {}

    public SubmitResignationResponse(String message, Long exitRequestId, String status, LocalDateTime submittedAt) {
        this.message = message;
        this.exitRequestId = exitRequestId;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getExitRequestId() { return exitRequestId; }
    public void setExitRequestId(Long exitRequestId) { this.exitRequestId = exitRequestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
