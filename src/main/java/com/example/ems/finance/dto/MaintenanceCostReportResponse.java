package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class MaintenanceCostReportResponse {
    private BigDecimal totalMaintenanceCost;
    private int assetsUnderMaintenance;
    private List<CategoryMaintenanceItem> categories;

    public MaintenanceCostReportResponse() {}

    public MaintenanceCostReportResponse(BigDecimal totalMaintenanceCost, int assetsUnderMaintenance, List<CategoryMaintenanceItem> categories) {
        this.totalMaintenanceCost = totalMaintenanceCost;
        this.assetsUnderMaintenance = assetsUnderMaintenance;
        this.categories = categories;
    }

    public BigDecimal getTotalMaintenanceCost() {
        return totalMaintenanceCost;
    }

    public void setTotalMaintenanceCost(BigDecimal totalMaintenanceCost) {
        this.totalMaintenanceCost = totalMaintenanceCost;
    }

    public int getAssetsUnderMaintenance() {
        return assetsUnderMaintenance;
    }

    public void setAssetsUnderMaintenance(int assetsUnderMaintenance) {
        this.assetsUnderMaintenance = assetsUnderMaintenance;
    }

    public List<CategoryMaintenanceItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryMaintenanceItem> categories) {
        this.categories = categories;
    }
}
