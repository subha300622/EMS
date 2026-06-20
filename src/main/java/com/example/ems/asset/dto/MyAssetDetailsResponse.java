package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MyAssetDetailsResponse {
    @Schema(example = "1")
    private Long assetId;
    @Schema(example = "EMP101")
    private String assetCode;
    @Schema(example = "string")
    private String assetName;
    @Schema(example = "string")
    private String category;
    @Schema(example = "string")
    private String brand;
    @Schema(example = "string")
    private String model;
    @Schema(example = "string")
    private String serialNumber;
    @Schema(example = "2026-06-19")
    private LocalDate purchaseDate;
    @Schema(example = "100.00")
    private BigDecimal purchasePrice;
    @Schema(example = "100.00")
    private BigDecimal currentValue;
    @Schema(example = "2026-06-19")
    private LocalDate assignedDate;
    @Schema(example = "string")
    private String assignedBy;
    @Schema(example = "Bangalore")
    private String location;
    @Schema(example = "string")
    private String condition;
    @Schema(example = "ACTIVE")
    private String warrantyStatus;
    @Schema(example = "2026-06-19")
    private LocalDate warrantyExpiryDate;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;

    public MyAssetDetailsResponse() {}

    public MyAssetDetailsResponse(Long assetId, String assetCode, String assetName, String category, String brand, String model, String serialNumber, LocalDate purchaseDate, BigDecimal purchasePrice, BigDecimal currentValue, LocalDate assignedDate, String assignedBy, String location, String condition, String warrantyStatus, LocalDate warrantyExpiryDate, String status, LocalDateTime createdAt) {
        this.assetId = assetId;
        this.assetCode = assetCode;
        this.assetName = assetName;
        this.category = category;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.currentValue = currentValue;
        this.assignedDate = assignedDate;
        this.assignedBy = assignedBy;
        this.location = location;
        this.condition = condition;
        this.warrantyStatus = warrantyStatus;
        this.warrantyExpiryDate = warrantyExpiryDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getWarrantyStatus() { return warrantyStatus; }
    public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }

    public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
