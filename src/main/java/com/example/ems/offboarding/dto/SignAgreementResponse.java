package com.example.ems.offboarding.dto;

import java.time.LocalDateTime;

public class SignAgreementResponse {

    private Long agreementId;
    private String agreementType;
    private LocalDateTime signedAt;
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
