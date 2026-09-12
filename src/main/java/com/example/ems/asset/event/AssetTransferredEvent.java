package com.example.ems.asset.event;

import java.time.LocalDateTime;

public record AssetTransferredEvent(
    Long assetId,
    Long organizationId,
    Long fromEmployeeId,
    Long toEmployeeId,
    Long fromDepartmentId,
    Long toDepartmentId,
    String reason,
    String performedBy,
    LocalDateTime timestamp
) {
    public AssetTransferredEvent(Long assetId, Long organizationId, Long fromEmployeeId, Long toEmployeeId, Long fromDepartmentId, Long toDepartmentId, String reason, String performedBy) {
        this(assetId, organizationId, fromEmployeeId, toEmployeeId, fromDepartmentId, toDepartmentId, reason, performedBy, LocalDateTime.now());
    }
}
