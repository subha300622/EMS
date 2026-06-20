package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class CategoryDepreciationItem {
    private String categoryName;
    private int assetCount;
    private BigDecimal depreciationAmount;

    public CategoryDepreciationItem() {}

    public CategoryDepreciationItem(String categoryName, int assetCount, BigDecimal depreciationAmount) {
        this.categoryName = categoryName;
        this.assetCount = assetCount;
        this.depreciationAmount = depreciationAmount;
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

    public BigDecimal getDepreciationAmount() {
        return depreciationAmount;
    }

    public void setDepreciationAmount(BigDecimal depreciationAmount) {
        this.depreciationAmount = depreciationAmount;
    }
}
