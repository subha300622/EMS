package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SaaS Registration Response Data")
public class SignupResponse {

    @Schema(example = "ORG0001", description = "Unique code assigned to the registered organization")
    private String organizationId;

    @Schema(example = "USR7707", description = "Unique ID assigned to the registered admin user")
    private String userId;

    @Schema(example = "false", description = "Indicates whether email verification is required before login")
    private boolean emailVerificationRequired;

    public SignupResponse() {}

    public SignupResponse(String organizationId, String userId, boolean emailVerificationRequired) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.emailVerificationRequired = emailVerificationRequired;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isEmailVerificationRequired() {
        return emailVerificationRequired;
    }

    public void setEmailVerificationRequired(boolean emailVerificationRequired) {
        this.emailVerificationRequired = emailVerificationRequired;
    }
}
