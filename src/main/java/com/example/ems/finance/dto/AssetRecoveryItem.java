package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class AssetRecoveryItem {
    private Long assetId;
    private String assetName;
    private String status;
    private BigDecimal deductionAmount;

    public AssetRecoveryItem() {}

    public AssetRecoveryItem(Long assetId, String assetName, String status, BigDecimal deductionAmount) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.status = status;
        this.deductionAmount = deductionAmount;
    }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getDeductionAmount() { return deductionAmount; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }
}
