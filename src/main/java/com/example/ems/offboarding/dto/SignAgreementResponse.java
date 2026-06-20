package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class SignAgreementResponse {

    @Schema(example = "1")
    private Long agreementId;
    @Schema(example = "string")
    private String agreementType;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime signedAt;
    @Schema(example = "ACTIVE")
    private String status;

    public SignAgreementResponse() {}

    public SignAgreementResponse(Long agreementId, String agreementType, LocalDateTime signedAt, String status) {
        this.agreementId = agreementId;
        this.agreementType = agreementType;
        this.signedAt = signedAt;
        this.status = status;
    }

    public Long getAgreementId() { return agreementId; }
    public void setAgreementId(Long agreementId) { this.agreementId = agreementId; }

    public String getAgreementType() { return agreementType; }
    public void setAgreementType(String agreementType) { this.agreementType = agreementType; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
