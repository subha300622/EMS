package com.example.ems.asset.dto;

import com.example.ems.asset.entity.MyAsset;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetDetailResponse {

    private Long id;
    private String assetTag;
    private String name;
    private String brand;
    private String category;
    private String serialNumber;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private BigDecimal currentValue;
    private BigDecimal depreciationPercentage;
    private LocalDate warrantyExpiry;
    private String vendor;
    private String location;
    private String status;

    public AssetDetailResponse() {}

    public AssetDetailResponse(MyAsset asset) {
        if (asset != null) {
            this.id = asset.getId();
            this.assetTag = asset.getAssetCode();
            this.name = asset.getAssetName();
            this.brand = asset.getBrand();
            this.category = asset.getCategory();
            this.serialNumber = asset.getSerialNumber();
            this.purchaseDate = asset.getPurchaseDate();
            this.purchaseValue = asset.getPurchasePrice();
            this.currentValue = asset.getCurrentValue();
            this.depreciationPercentage = asset.getDepreciationPercentage();
            this.warrantyExpiry = asset.getWarrantyExpiryDate();
            this.vendor = asset.getVendor();
            this.location = asset.getLocation();
            
            // Map internal status to client-facing status
            String internalStatus = asset.getStatus();
            if ("UNASSIGNED".equalsIgnoreCase(internalStatus) || "RETURNED".equalsIgnoreCase(internalStatus) || "AVAILABLE".equalsIgnoreCase(internalStatus)) {
                this.status = "AVAILABLE";
            } else {
                this.status = internalStatus;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
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

    public BigDecimal getDepreciationPercentage() {
        return depreciationPercentage;
    }

    public void setDepreciationPercentage(BigDecimal depreciationPercentage) {
        this.depreciationPercentage = depreciationPercentage;
    }

    public LocalDate getWarrantyExpiry() {
        return warrantyExpiry;
    }

    public void setWarrantyExpiry(LocalDate warrantyExpiry) {
        this.warrantyExpiry = warrantyExpiry;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
