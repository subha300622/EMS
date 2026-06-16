package com.example.ems.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateAssetRequest {

    @NotBlank(message = "Asset category is required")
    private String assetCategory;

    @NotBlank(message = "Requested model is required")
    private String requestedModel;

    @NotBlank(message = "Business reason is required")
    private String businessReason;

    private String priority = "MEDIUM"; // default

    @NotNull(message = "Required by date is required")
    private LocalDate requiredByDate;

    public CreateAssetRequest() {}

    public CreateAssetRequest(String assetCategory, String requestedModel, String businessReason, String priority, LocalDate requiredByDate) {
        this.assetCategory = assetCategory;
        this.requestedModel = requestedModel;
        this.businessReason = businessReason;
        this.priority = priority;
        this.requiredByDate = requiredByDate;
    }

    public String getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory) {
        this.assetCategory = assetCategory;
    }

    public String getRequestedModel() {
        return requestedModel;
    }

    public void setRequestedModel(String requestedModel) {
        this.requestedModel = requestedModel;
    }

    public String getBusinessReason() {
        return businessReason;
    }

    public void setBusinessReason(String businessReason) {
        this.businessReason = businessReason;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getRequiredByDate() {
        return requiredByDate;
    }

    public void setRequiredByDate(LocalDate requiredByDate) {
        this.requiredByDate = requiredByDate;
    }
}
