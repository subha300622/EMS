package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CategoryAssetItem {
    private Long assetId;
    private String assetTag;
    private String assetName;
    private BigDecimal purchaseValue;
    private BigDecimal currentValue;
    private LocalDate assignedDate;
    private String status;

    public CategoryAssetItem() {}

    public CategoryAssetItem(Long assetId, String assetTag, String assetName, BigDecimal purchaseValue, BigDecimal currentValue, LocalDate assignedDate, String status) {
        this.assetId = assetId;
        this.assetTag = assetTag;
        this.assetName = assetName;
        this.purchaseValue = purchaseValue;
        this.currentValue = currentValue;
        this.assignedDate = assignedDate;
        this.status = status;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(BigDecimal purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
