package com.example.ems.common.dto.manager;

public record PendingApprovalDto(
    Long id,
    Long employeeId,
    String employeeName,
    String approvalType,
    String submittedDate,
    ApprovalStatus status
) {}
