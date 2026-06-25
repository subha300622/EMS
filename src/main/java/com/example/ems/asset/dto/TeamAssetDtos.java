package com.example.ems.asset.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class TeamAssetDtos {
    
    private TeamAssetDtos() {}

    public record DashboardResponse(
        boolean success,
        DashboardData data
    ) {}

    public record DashboardData(
        long teamMembersWithAssets,
        long totalAssets,
        BigDecimal totalAssetValue,
        String totalAssetValueDisplay,
        long pendingReturns,
        long pendingRequests
    ) {}

    public record InventoryItem(
        Long assetAssignmentId,
        Long assetId,
        String assetCode,
        String assetName,
        String category,
        String serialNumber,
        BigDecimal value,
        String status,
        EmployeeSummary employee,
        LocalDate assignedDate
    ) {}

    public record EmployeeSummary(
        Long employeeId,
        String employeeCode,
        String employeeName,
        String designation
    ) {}

    public record DetailResponse(
        Long assetId,
        String assetCode,
        String assetName,
        String category,
        String department,
        String serialNumber,
        String status,
        BigDecimal value,
        String condition,
        String notes,
        LocalDate assignedSince,
        EmployeeSummary assignedTo
    ) {}

    public record TimelineEvent(
        String eventType,
        LocalDate date,
        String description
    ) {}

    public record RequestItem(
        Long requestId,
        Long employeeId,
        String employeeName,
        String employeeCode,
        String assetCategory,
        String assetName,
        String reason,
        LocalDate requestedOn,
        String status
    ) {}

    public record ReturnRequestItem(
        Long returnRequestId,
        Long assetId,
        String assetCode,
        String assetName,
        Long employeeId,
        String employeeName,
        LocalDate requestedDate,
        String returnReason,
        String status
    ) {}

    public record AnalyticsResponse(
        boolean success,
        AnalyticsData data
    ) {}

    public record AnalyticsData(
        List<CategoryCountItem> assetsByCategory,
        long assignedAssets,
        long pendingReturns,
        BigDecimal totalAssetValue
    ) {}

    public record CategoryCountItem(
        String category,
        long count
    ) {}

    public record ApprovalRequest(
        String managerRemarks
    ) {}

    public record ReturnApprovalRequest(
        String remarks
    ) {}

    public record ActionResponse(
        boolean success,
        String message
    ) {}
}
