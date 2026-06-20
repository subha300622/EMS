package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetDto {
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
    @Schema(example = "1")
    private Long assignedToEmployeeId;

    public AssetDto() {}

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

    public Long getAssignedToEmployeeId() { return assignedToEmployeeId; }
    public void setAssignedToEmployeeId(Long assignedToEmployeeId) { this.assignedToEmployeeId = assignedToEmployeeId; }
}
