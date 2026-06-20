package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReplacementDueAssetItem {
    private Long assetId;
    private String assetTag;
    private String assetName;
    private String category;
    private LocalDate purchaseDate;
    private BigDecimal currentValue;
    private int yearsInUse;
    private String replacementPriority;

    public ReplacementDueAssetItem() {}

    public ReplacementDueAssetItem(Long assetId, String assetTag, String assetName, String category, LocalDate purchaseDate, BigDecimal currentValue, int yearsInUse, String replacementPriority) {
        this.assetId = assetId;
        this.assetTag = assetTag;
        this.assetName = assetName;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.currentValue = currentValue;
        this.yearsInUse = yearsInUse;
        this.replacementPriority = replacementPriority;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public int getYearsInUse() {
        return yearsInUse;
    }

    public void setYearsInUse(int yearsInUse) {
        this.yearsInUse = yearsInUse;
    }

    public String getReplacementPriority() {
        return replacementPriority;
    }

    public void setReplacementPriority(String replacementPriority) {
        this.replacementPriority = replacementPriority;
    }
}
