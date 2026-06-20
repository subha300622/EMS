package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AssetReturnConfirmResponse {

    @Schema(example = "1")
    private Long assetId;
    @Schema(example = "string")
    private String assetName;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String verifiedBy;

    public AssetReturnConfirmResponse() {}

    public AssetReturnConfirmResponse(Long assetId, String assetName, String status, String verifiedBy) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.status = status;
        this.verifiedBy = verifiedBy;
    }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
}
