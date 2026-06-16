package com.example.ems.offboarding.dto;

public class AssetReturnConfirmResponse {

    private Long assetId;
    private String assetName;
    private String status;
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
