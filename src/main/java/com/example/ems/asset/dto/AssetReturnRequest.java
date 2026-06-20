package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssetReturnRequest {

    @NotNull(message = "Offboarding ID is required")
    @Schema(example = "1")
    private Long offboardingId;

    @NotBlank(message = "Asset name is required")
    @Schema(example = "string")
    private String assetName;

    @Schema(example = "string")
    private String serialNumber;

    @NotBlank(message = "Return status is required")
    @Schema(example = "ACTIVE")
    private String returnStatus; // RETURNED, DAMAGED

    public Long getOffboardingId() { return offboardingId; }
    public void setOffboardingId(Long offboardingId) { this.offboardingId = offboardingId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
}
