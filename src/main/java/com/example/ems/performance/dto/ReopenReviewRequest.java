package com.example.ems.performance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reopen Rejected Performance Review Payload")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReopenReviewRequest {

    @Schema(description = "Justification for reopening review for correction", example = "Correction requested for attendance data update.")
    private String reason;

    @Schema(description = "Idempotency key to safely retry reopen action", example = "reopen-act-4b5c6d")
    private String idempotencyKey;

    public ReopenReviewRequest() {}

    public ReopenReviewRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
