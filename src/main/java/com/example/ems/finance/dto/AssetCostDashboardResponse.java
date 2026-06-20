package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetCostDashboardResponse {
    private BigDecimal totalAssetValue;
    private BigDecimal annualDepreciation;
    private BigDecimal maintenanceCost;
    private int replacementDue;
    private int assetCount;
    private LocalDate asOfDate;

    public AssetCostDashboardResponse() {}

    public AssetCostDashboardResponse(BigDecimal totalAssetValue, BigDecimal annualDepreciation, BigDecimal maintenanceCost, int replacementDue, int assetCount, LocalDate asOfDate) {
        this.totalAssetValue = totalAssetValue;
        this.annualDepreciation = annualDepreciation;
        this.maintenanceCost = maintenanceCost;
        this.replacementDue = replacementDue;
        this.assetCount = assetCount;
        this.asOfDate = asOfDate;
    }

    public BigDecimal getTotalAssetValue() {
        return totalAssetValue;
    }

    public void setTotalAssetValue(BigDecimal totalAssetValue) {
        this.totalAssetValue = totalAssetValue;
    }

    public BigDecimal getAnnualDepreciation() {
        return annualDepreciation;
    }

    public void setAnnualDepreciation(BigDecimal annualDepreciation) {
        this.annualDepreciation = annualDepreciation;
    }

    public BigDecimal getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(BigDecimal maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public int getReplacementDue() {
        return replacementDue;
    }

    public void setReplacementDue(int replacementDue) {
        this.replacementDue = replacementDue;
    }

    public int getAssetCount() {
        return assetCount;
    }

    public void setAssetCount(int assetCount) {
        this.assetCount = assetCount;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }
}
