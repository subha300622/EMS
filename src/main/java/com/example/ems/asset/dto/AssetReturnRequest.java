package com.example.ems.asset.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssetReturnRequest {

    @NotNull(message = "Offboarding ID is required")
    private Long offboardingId;

    @NotBlank(message = "Asset name is required")
    private String assetName;

    private String serialNumber;

    @NotBlank(message = "Return status is required")
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
