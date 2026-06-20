package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetFinancialDetailsResponse {
    private Long assetId;
    private String assetTag;
    private String assetName;
    private String category;
    private String brand;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private BigDecimal currentValue;
    private BigDecimal bookValue;
    private BigDecimal annualDepreciation;
    private BigDecimal depreciationRate;
    private BigDecimal maintenanceCost;
    private LocalDate warrantyExpiry;
    private boolean replacementDue;
    private String status;

    public AssetFinancialDetailsResponse() {}

    public AssetFinancialDetailsResponse(Long assetId, String assetTag, String assetName, String category, String brand, LocalDate purchaseDate, BigDecimal purchaseValue, BigDecimal currentValue, BigDecimal bookValue, BigDecimal annualDepreciation, BigDecimal depreciationRate, BigDecimal maintenanceCost, LocalDate warrantyExpiry, boolean replacementDue, String status) {
        this.assetId = assetId;
        this.assetTag = assetTag;
        this.assetName = assetName;
        this.category = category;
        this.brand = brand;
        this.purchaseDate = purchaseDate;
        this.purchaseValue = purchaseValue;
        this.currentValue = currentValue;
        this.bookValue = bookValue;
        this.annualDepreciation = annualDepreciation;
        this.depreciationRate = depreciationRate;
        this.maintenanceCost = maintenanceCost;
        this.warrantyExpiry = warrantyExpiry;
        this.replacementDue = replacementDue;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
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

    public BigDecimal getBookValue() {
        return bookValue;
    }

    public void setBookValue(BigDecimal bookValue) {
        this.bookValue = bookValue;
    }

    public BigDecimal getAnnualDepreciation() {
        return annualDepreciation;
    }

    public void setAnnualDepreciation(BigDecimal annualDepreciation) {
        this.annualDepreciation = annualDepreciation;
    }

    public BigDecimal getDepreciationRate() {
        return depreciationRate;
    }

    public void setDepreciationRate(BigDecimal depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public BigDecimal getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(BigDecimal maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public LocalDate getWarrantyExpiry() {
        return warrantyExpiry;
    }

    public void setWarrantyExpiry(LocalDate warrantyExpiry) {
        this.warrantyExpiry = warrantyExpiry;
    }

    public boolean isReplacementDue() {
        return replacementDue;
    }

    public void setReplacementDue(boolean replacementDue) {
        this.replacementDue = replacementDue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
