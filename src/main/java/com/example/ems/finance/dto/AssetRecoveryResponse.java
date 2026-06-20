package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class AssetRecoveryResponse {
    private Long employeeId;
    private int returnedAssets;
    private int pendingAssets;
    private BigDecimal assetDeduction;
    private List<AssetRecoveryItem> assets;

    public AssetRecoveryResponse() {}

    public AssetRecoveryResponse(Long employeeId, int returnedAssets, int pendingAssets, BigDecimal assetDeduction, List<AssetRecoveryItem> assets) {
        this.employeeId = employeeId;
        this.returnedAssets = returnedAssets;
        this.pendingAssets = pendingAssets;
        this.assetDeduction = assetDeduction;
        this.assets = assets;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public int getReturnedAssets() { return returnedAssets; }
    public void setReturnedAssets(int returnedAssets) { this.returnedAssets = returnedAssets; }

    public int getPendingAssets() { return pendingAssets; }
    public void setPendingAssets(int pendingAssets) { this.pendingAssets = pendingAssets; }

    public BigDecimal getAssetDeduction() { return assetDeduction; }
    public void setAssetDeduction(BigDecimal assetDeduction) { this.assetDeduction = assetDeduction; }

    public List<AssetRecoveryItem> getAssets() { return assets; }
    public void setAssets(List<AssetRecoveryItem> assets) { this.assets = assets; }
}
