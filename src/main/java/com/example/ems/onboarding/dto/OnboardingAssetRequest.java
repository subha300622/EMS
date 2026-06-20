package com.example.ems.onboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OnboardingAssetRequest {

    @NotNull(message = "Onboarding ID is required")
    @Schema(example = "1")
    private Long onboardingId;

    @NotBlank(message = "Asset name is required")
    @Schema(example = "string")
    private String assetName;

    @Schema(example = "string")
    private String serialNumber;

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
}
