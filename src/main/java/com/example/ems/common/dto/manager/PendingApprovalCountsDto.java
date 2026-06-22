package com.example.ems.common.dto.manager;

public record PendingApprovalCountsDto(
    Long leaveRequests,
    Long expenseApprovals,
    Long shiftChanges,
    Long goalApprovals,
    Long assetRequests,
    Long total
) {}
