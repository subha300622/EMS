package com.example.ems.asset.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AssetRequestResponse {
    private Long id;
    private String requestNumber;
    private String assetCategory;
    private String requestedModel;
    private String businessReason;
    private String priority;
    private LocalDate requiredByDate;
    private String managerComments;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDate expectedApprovalDate;
    private String currentApprover;

    public AssetRequestResponse() {}

    public AssetRequestResponse(Long id, String requestNumber, String assetCategory, String requestedModel, String businessReason, String priority, LocalDate requiredByDate, String managerComments, String status, LocalDateTime requestedAt, LocalDate expectedApprovalDate, String currentApprover) {
        this.id = id;
        this.requestNumber = requestNumber;
        this.assetCategory = assetCategory;
        this.requestedModel = requestedModel;
        this.businessReason = businessReason;
        this.priority = priority;
        this.requiredByDate = requiredByDate;
        this.managerComments = managerComments;
        this.status = status;
        this.requestedAt = requestedAt;
        this.expectedApprovalDate = expectedApprovalDate;
        this.currentApprover = currentApprover;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public String getAssetCategory() { return assetCategory; }
    public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }

    public String getRequestedModel() { return requestedModel; }
    public void setRequestedModel(String requestedModel) { this.requestedModel = requestedModel; }

    public String getBusinessReason() { return businessReason; }
    public void setBusinessReason(String businessReason) { this.businessReason = businessReason; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDate getRequiredByDate() { return requiredByDate; }
    public void setRequiredByDate(LocalDate requiredByDate) { this.requiredByDate = requiredByDate; }

    public String getManagerComments() { return managerComments; }
    public void setManagerComments(String managerComments) { this.managerComments = managerComments; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDate getExpectedApprovalDate() { return expectedApprovalDate; }
    public void setExpectedApprovalDate(LocalDate expectedApprovalDate) { this.expectedApprovalDate = expectedApprovalDate; }

    public String getCurrentApprover() { return currentApprover; }
    public void setCurrentApprover(String currentApprover) { this.currentApprover = currentApprover; }
}
