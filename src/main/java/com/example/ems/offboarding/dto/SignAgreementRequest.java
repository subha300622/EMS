package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SignAgreementRequest {

    @NotBlank(message = "Agreement type is required")
    @Schema(example = "string")
    private String agreementType;

    @NotNull(message = "Accepted must be explicitly set")
    @Schema(example = "true")
    private Boolean accepted;

    public String getAgreementType() { return agreementType; }
    public void setAgreementType(String agreementType) { this.agreementType = agreementType; }

    public Boolean getAccepted() { return accepted; }
    public void setAccepted(Boolean accepted) { this.accepted = accepted; }
}
