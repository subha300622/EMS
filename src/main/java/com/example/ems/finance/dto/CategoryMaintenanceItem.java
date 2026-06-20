package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class CategoryMaintenanceItem {
    private String categoryName;
    private BigDecimal maintenanceCost;

    public CategoryMaintenanceItem() {}

    public CategoryMaintenanceItem(String categoryName, BigDecimal maintenanceCost) {
        this.categoryName = categoryName;
        this.maintenanceCost = maintenanceCost;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(BigDecimal maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }
}
