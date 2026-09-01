package com.example.ems.asset.dto;

import com.example.ems.asset.entity.AssetCondition;
import com.example.ems.asset.entity.AssetEventType;
import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.entity.AssignmentStatus;
import com.example.ems.asset.entity.MaintenanceStatus;
import com.example.ems.asset.entity.MaintenanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AssetDtos {

    // --- Category DTOs ---

    public static class CreateCategoryRequest {
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        private String categoryName;

        @NotBlank(message = "Category code is required")
        @Size(max = 50, message = "Category code cannot exceed 50 characters")
        private String categoryCode;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CategoryResponse {
        private Long id;
        private String categoryName;
        private String categoryCode;
        private String description;
        private boolean active;

        public CategoryResponse(Long id, String categoryName, String categoryCode, String description, boolean active) {
            this.id = id;
            this.categoryName = categoryName;
            this.categoryCode = categoryCode;
            this.description = description;
            this.active = active;
        }

        public Long getId() { return id; }
        public String getCategoryName() { return categoryName; }
        public String getCategoryCode() { return categoryCode; }
        public String getDescription() { return description; }
        public boolean isActive() { return active; }
    }

    // --- Location DTOs ---

    public static class CreateLocationRequest {
        @NotBlank(message = "Location name is required")
        @Size(max = 100, message = "Location name cannot exceed 100 characters")
        private String locationName;

        @NotBlank(message = "Location code is required")
        @Size(max = 50, message = "Location code cannot exceed 50 characters")
        private String locationCode;

        private Long parentId;

        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getLocationCode() { return locationCode; }
        public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
    }

    public static class LocationResponse {
        private Long id;
        private String locationName;
        private String locationCode;
        private Long parentId;
        private String parentName;
        private boolean active;

        public LocationResponse(Long id, String locationName, String locationCode, Long parentId, String parentName, boolean active) {
            this.id = id;
            this.locationName = locationName;
            this.locationCode = locationCode;
            this.parentId = parentId;
            this.parentName = parentName;
            this.active = active;
        }

        public Long getId() { return id; }
        public String getLocationName() { return locationName; }
        public String getLocationCode() { return locationCode; }
        public Long getParentId() { return parentId; }
        public String getParentName() { return parentName; }
        public boolean isActive() { return active; }
    }

    // --- Asset Master DTOs ---

    public static class CreateAssetRequest {
        @NotBlank(message = "Asset name is required")
        @Size(max = 255, message = "Asset name cannot exceed 255 characters")
        private String assetName;

        @NotBlank(message = "Asset code is required")
        @Size(max = 50, message = "Asset code cannot exceed 50 characters")
        private String assetCode;

        @NotNull(message = "Category ID is required")
        private Long categoryId;

        @NotNull(message = "Location ID is required")
        private Long locationId;

        @Size(max = 100, message = "Serial number cannot exceed 100 characters")
        private String serialNumber;

        private String brand;
        private String model;

        @NotNull(message = "Purchase date is required")
        @PastOrPresent(message = "Purchase date cannot be in the future")
        private LocalDate purchaseDate;

        @NotNull(message = "Purchase cost is required")
        @DecimalMin(value = "0.0", message = "Purchase cost cannot be negative")
        private BigDecimal purchaseCost;

        private String warrantyStatus;
        private LocalDate warrantyExpiryDate;
        private String vendor;
        private String description;

        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        public String getAssetCode() { return assetCode; }
        public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public LocalDate getPurchaseDate() { return purchaseDate; }
        public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
        public BigDecimal getPurchaseCost() { return purchaseCost; }
        public void setPurchaseCost(BigDecimal purchaseCost) { this.purchaseCost = purchaseCost; }
        public String getWarrantyStatus() { return warrantyStatus; }
        public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
        public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
        public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }
        public String getVendor() { return vendor; }
        public void setVendor(String vendor) { this.vendor = vendor; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class EditAssetRequest {
        @NotBlank(message = "Asset name is required")
        private String assetName;

        @NotNull(message = "Category ID is required")
        private Long categoryId;

        private String brand;
        private String model;

        @NotNull(message = "Purchase cost is required")
        @DecimalMin(value = "0.0", message = "Purchase cost cannot be negative")
        private BigDecimal purchaseCost;

        private String warrantyStatus;
        private LocalDate warrantyExpiryDate;
        private String vendor;
        private String description;

        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public BigDecimal getPurchaseCost() { return purchaseCost; }
        public void setPurchaseCost(BigDecimal purchaseCost) { this.purchaseCost = purchaseCost; }
        public String getWarrantyStatus() { return warrantyStatus; }
        public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
        public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
        public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }
        public String getVendor() { return vendor; }
        public void setVendor(String vendor) { this.vendor = vendor; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class AssetResponse {
        private Long id;
        private String assetCode;
        private String assetName;
        private Long categoryId;
        private String categoryName;
        private Long locationId;
        private String locationName;
        private String serialNumber;
        private String brand;
        private String model;
        private LocalDate purchaseDate;
        private BigDecimal purchaseCost;
        private BigDecimal currentValue;
        private AssetStatus status;
        private AssetCondition condition;
        private String warrantyStatus;
        private LocalDate warrantyExpiryDate;
        private String vendor;
        private String description;
        private boolean isDeleted;
        private Long version;
        private LocalDateTime createdAt;

        public AssetResponse(Long id, String assetCode, String assetName, Long categoryId, String categoryName,
                             Long locationId, String locationName, String serialNumber, String brand, String model,
                             LocalDate purchaseDate, BigDecimal purchaseCost, BigDecimal currentValue, AssetStatus status,
                             AssetCondition condition, String warrantyStatus, LocalDate warrantyExpiryDate,
                             String vendor, String description, boolean isDeleted, Long version, LocalDateTime createdAt) {
            this.id = id;
            this.assetCode = assetCode;
            this.assetName = assetName;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.locationId = locationId;
            this.locationName = locationName;
            this.serialNumber = serialNumber;
            this.brand = brand;
            this.model = model;
            this.purchaseDate = purchaseDate;
            this.purchaseCost = purchaseCost;
            this.currentValue = currentValue;
            this.status = status;
            this.condition = condition;
            this.warrantyStatus = warrantyStatus;
            this.warrantyExpiryDate = warrantyExpiryDate;
            this.vendor = vendor;
            this.description = description;
            this.isDeleted = isDeleted;
            this.version = version;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getAssetCode() { return assetCode; }
        public String getAssetName() { return assetName; }
        public Long getCategoryId() { return categoryId; }
        public String getCategoryName() { return categoryName; }
        public Long getLocationId() { return locationId; }
        public String getLocationName() { return locationName; }
        public String getSerialNumber() { return serialNumber; }
        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public LocalDate getPurchaseDate() { return purchaseDate; }
        public BigDecimal getPurchaseCost() { return purchaseCost; }
        public BigDecimal getCurrentValue() { return currentValue; }
        public AssetStatus getStatus() { return status; }
        public AssetCondition getCondition() { return condition; }
        public String getWarrantyStatus() { return warrantyStatus; }
        public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
        public String getVendor() { return vendor; }
        public String getDescription() { return description; }
        public boolean isDeleted() { return isDeleted; }
        public Long getVersion() { return version; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    // --- Assignment & Action DTOs ---

    public static class AssignAssetRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Location ID is required")
        private Long locationId;

        @NotNull(message = "Assigned date is required")
        @PastOrPresent(message = "Assigned date cannot be in the future")
        private LocalDate assignedDate;

        private LocalDate expectedReturnDate;
        private String remarks;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public LocalDate getAssignedDate() { return assignedDate; }
        public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
        public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
        public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class TransferAssetRequest {
        @NotNull(message = "Target employee ID is required")
        private Long toEmployeeId;

        @NotNull(message = "Target location ID is required")
        private Long toLocationId;

        @NotNull(message = "Transfer date is required")
        @PastOrPresent(message = "Transfer date cannot be in the future")
        private LocalDate transferDate;

        private String remarks;

        public Long getToEmployeeId() { return toEmployeeId; }
        public void setToEmployeeId(Long toEmployeeId) { this.toEmployeeId = toEmployeeId; }
        public Long getToLocationId() { return toLocationId; }
        public void setToLocationId(Long toLocationId) { this.toLocationId = toLocationId; }
        public LocalDate getTransferDate() { return transferDate; }
        public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class ReturnAssetRequest {
        @NotNull(message = "Return date is required")
        @PastOrPresent(message = "Return date cannot be in the future")
        private LocalDate returnDate;

        @NotNull(message = "Return condition is required")
        private AssetCondition condition;

        private String remarks;

        public LocalDate getReturnDate() { return returnDate; }
        public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
        public AssetCondition getCondition() { return condition; }
        public void setCondition(AssetCondition condition) { this.condition = condition; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class RetireAssetRequest {
        @NotBlank(message = "Reason is required")
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class DisposeAssetRequest {
        @NotBlank(message = "Disposal reason is required")
        private String disposalReason;

        @DecimalMin(value = "0.0", message = "Disposal cost cannot be negative")
        private BigDecimal disposalCost;

        @NotNull(message = "Disposal date is required")
        @PastOrPresent(message = "Disposal date cannot be in the future")
        private LocalDate disposalDate;

        public String getDisposalReason() { return disposalReason; }
        public void setDisposalReason(String disposalReason) { this.disposalReason = disposalReason; }
        public BigDecimal getDisposalCost() { return disposalCost; }
        public void setDisposalCost(BigDecimal disposalCost) { this.disposalCost = disposalCost; }
        public LocalDate getDisposalDate() { return disposalDate; }
        public void setDisposalDate(LocalDate disposalDate) { this.disposalDate = disposalDate; }
    }

    // --- Maintenance DTOs ---

    public static class CreateMaintenanceRequest {
        @NotNull(message = "Maintenance type is required")
        private MaintenanceType maintenanceType;

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;

        @NotNull(message = "Scheduled date is required")
        private LocalDate scheduledDate;

        @DecimalMin(value = "0.0", message = "Estimated cost cannot be negative")
        private BigDecimal estimatedCost;

        private String vendor;
        private String technician;
        private String remarks;

        public MaintenanceType getMaintenanceType() { return maintenanceType; }
        public void setMaintenanceType(MaintenanceType maintenanceType) { this.maintenanceType = maintenanceType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getScheduledDate() { return scheduledDate; }
        public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
        public BigDecimal getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
        public String getVendor() { return vendor; }
        public void setVendor(String vendor) { this.vendor = vendor; }
        public String getTechnician() { return technician; }
        public void setTechnician(String technician) { this.technician = technician; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class CompleteMaintenanceRequest {
        @NotNull(message = "Completed date is required")
        @PastOrPresent(message = "Completed date cannot be in the future")
        private LocalDate completedDate;

        @DecimalMin(value = "0.0", message = "Actual cost cannot be negative")
        private BigDecimal actualCost;

        @NotBlank(message = "Result is required")
        private String result;

        private String remarks;

        public LocalDate getCompletedDate() { return completedDate; }
        public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }
        public BigDecimal getActualCost() { return actualCost; }
        public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    // --- Response DTOs ---

    public static class AssetAssignmentResponse {
        private Long id;
        private Long assetId;
        private String assetCode;
        private String assetName;
        private Long employeeId;
        private String employeeName;
        private Long locationId;
        private String locationName;
        private LocalDate assignedDate;
        private LocalDate expectedReturnDate;
        private LocalDate returnedDate;
        private AssignmentStatus status;
        private String remarks;

        public AssetAssignmentResponse(Long id, Long assetId, String assetCode, String assetName, Long employeeId,
                                       String employeeName, Long locationId, String locationName, LocalDate assignedDate,
                                       LocalDate expectedReturnDate, LocalDate returnedDate, AssignmentStatus status, String remarks) {
            this.id = id;
            this.assetId = assetId;
            this.assetCode = assetCode;
            this.assetName = assetName;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.locationId = locationId;
            this.locationName = locationName;
            this.assignedDate = assignedDate;
            this.expectedReturnDate = expectedReturnDate;
            this.returnedDate = returnedDate;
            this.status = status;
            this.remarks = remarks;
        }

        public Long getId() { return id; }
        public Long getAssetId() { return assetId; }
        public String getAssetCode() { return assetCode; }
        public String getAssetName() { return assetName; }
        public Long getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public Long getLocationId() { return locationId; }
        public String getLocationName() { return locationName; }
        public LocalDate getAssignedDate() { return assignedDate; }
        public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
        public LocalDate getReturnedDate() { return returnedDate; }
        public AssignmentStatus getStatus() { return status; }
        public String getRemarks() { return remarks; }
    }

    public static class AssetMaintenanceResponse {
        private Long id;
        private Long assetId;
        private String assetCode;
        private String assetName;
        private MaintenanceType maintenanceType;
        private String description;
        private MaintenanceStatus status;
        private LocalDate scheduledDate;
        private LocalDate startDate;
        private LocalDate completedDate;
        private BigDecimal estimatedCost;
        private BigDecimal actualCost;
        private String vendor;
        private String technician;
        private String result;
        private String remarks;

        public AssetMaintenanceResponse(Long id, Long assetId, String assetCode, String assetName, MaintenanceType maintenanceType,
                                        String description, MaintenanceStatus status, LocalDate scheduledDate, LocalDate startDate,
                                        LocalDate completedDate, BigDecimal estimatedCost, BigDecimal actualCost, String vendor,
                                        String technician, String result, String remarks) {
            this.id = id;
            this.assetId = assetId;
            this.assetCode = assetCode;
            this.assetName = assetName;
            this.maintenanceType = maintenanceType;
            this.description = description;
            this.status = status;
            this.scheduledDate = scheduledDate;
            this.startDate = startDate;
            this.completedDate = completedDate;
            this.estimatedCost = estimatedCost;
            this.actualCost = actualCost;
            this.vendor = vendor;
            this.technician = technician;
            this.result = result;
            this.remarks = remarks;
        }

        public Long getId() { return id; }
        public Long getAssetId() { return assetId; }
        public String getAssetCode() { return assetCode; }
        public String getAssetName() { return assetName; }
        public MaintenanceType getMaintenanceType() { return maintenanceType; }
        public String getDescription() { return description; }
        public MaintenanceStatus getStatus() { return status; }
        public LocalDate getScheduledDate() { return scheduledDate; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getCompletedDate() { return completedDate; }
        public BigDecimal getEstimatedCost() { return estimatedCost; }
        public BigDecimal getActualCost() { return actualCost; }
        public String getVendor() { return vendor; }
        public String getTechnician() { return technician; }
        public String getResult() { return result; }
        public String getRemarks() { return remarks; }
    }

    public static class AssetHistoryResponse {
        private Long id;
        private Long assetId;
        private AssetEventType eventType;
        private String oldStatus;
        private String newStatus;
        private Long fromEmployeeId;
        private String fromEmployeeName;
        private Long toEmployeeId;
        private String toEmployeeName;
        private Long fromLocationId;
        private String fromLocationName;
        private Long toLocationId;
        private String toLocationName;
        private String performedBy;
        private LocalDateTime performedAt;
        private String referenceId;
        private String remarks;

        public AssetHistoryResponse(Long id, Long assetId, AssetEventType eventType, String oldStatus, String newStatus,
                                    Long fromEmployeeId, String fromEmployeeName, Long toEmployeeId, String toEmployeeName,
                                    Long fromLocationId, String fromLocationName, Long toLocationId, String toLocationName,
                                    String performedBy, LocalDateTime performedAt, String referenceId, String remarks) {
            this.id = id;
            this.assetId = assetId;
            this.eventType = eventType;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.fromEmployeeId = fromEmployeeId;
            this.fromEmployeeName = fromEmployeeName;
            this.toEmployeeId = toEmployeeId;
            this.toEmployeeName = toEmployeeName;
            this.fromLocationId = fromLocationId;
            this.fromLocationName = fromLocationName;
            this.toLocationId = toLocationId;
            this.toLocationName = toLocationName;
            this.performedBy = performedBy;
            this.performedAt = performedAt;
            this.referenceId = referenceId;
            this.remarks = remarks;
        }

        public Long getId() { return id; }
        public Long getAssetId() { return assetId; }
        public AssetEventType getEventType() { return eventType; }
        public String getOldStatus() { return oldStatus; }
        public String getNewStatus() { return newStatus; }
        public Long getFromEmployeeId() { return fromEmployeeId; }
        public String getFromEmployeeName() { return fromEmployeeName; }
        public Long getToEmployeeId() { return toEmployeeId; }
        public String getToEmployeeName() { return toEmployeeName; }
        public Long getFromLocationId() { return fromLocationId; }
        public String getFromLocationName() { return fromLocationName; }
        public Long getToLocationId() { return toLocationId; }
        public String getToLocationName() { return toLocationName; }
        public String getPerformedBy() { return performedBy; }
        public LocalDateTime getPerformedAt() { return performedAt; }
        public String getReferenceId() { return referenceId; }
        public String getRemarks() { return remarks; }
    }

    public static class AssetActionResultResponse {
        private Long requestId;
        private Long assetId;
        private String requestType;
        private String status;
        private boolean requiresApproval;
        private Long approvalInstanceId;
        private String message;

        public AssetActionResultResponse(Long requestId, Long assetId, String requestType, String status,
                                           boolean requiresApproval, Long approvalInstanceId, String message) {
            this.requestId = requestId;
            this.assetId = assetId;
            this.requestType = requestType;
            this.status = status;
            this.requiresApproval = requiresApproval;
            this.approvalInstanceId = approvalInstanceId;
            this.message = message;
        }

        public AssetActionResultResponse(Long requestId, String status, String message) {
            this.requestId = requestId;
            this.status = status;
            this.message = message;
        }

        public Long getRequestId() { return requestId; }
        public Long getAssetId() { return assetId; }
        public String getRequestType() { return requestType; }
        public String getStatus() { return status; }
        public boolean isRequiresApproval() { return requiresApproval; }
        public Long getApprovalInstanceId() { return approvalInstanceId; }
        public String getMessage() { return message; }
    }
}
