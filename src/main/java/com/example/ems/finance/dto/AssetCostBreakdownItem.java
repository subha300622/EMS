package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class AssetCostBreakdownItem {
    private Long categoryId;
    private String categoryName;
    private int assetCount;
    private BigDecimal totalValue;
    private BigDecimal annualDepreciation;
    private BigDecimal bookValue;
    private String status;

    public AssetCostBreakdownItem() {}

    public AssetCostBreakdownItem(Long categoryId, String categoryName, int assetCount, BigDecimal totalValue, BigDecimal annualDepreciation, BigDecimal bookValue, String status) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.assetCount = assetCount;
        this.totalValue = totalValue;
        this.annualDepreciation = annualDepreciation;
        this.bookValue = bookValue;
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getAssetCount() {
        return assetCount;
    }

    public void setAssetCount(int assetCount) {
        this.assetCount = assetCount;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getAnnualDepreciation() {
        return annualDepreciation;
    }

    public void setAnnualDepreciation(BigDecimal annualDepreciation) {
        this.annualDepreciation = annualDepreciation;
    }

    public BigDecimal getBookValue() {
        return bookValue;
    }

    public void setBookValue(BigDecimal bookValue) {
        this.bookValue = bookValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
