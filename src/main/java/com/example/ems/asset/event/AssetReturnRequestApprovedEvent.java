package com.example.ems.asset.event;

import java.time.LocalDateTime;

public record AssetReturnRequestApprovedEvent(
    Long returnRequestId,
    Long assetId,
    Long organizationId,
    Long returnedById,
    String comment,
    String approvedBy,
    LocalDateTime timestamp
) {
    public AssetReturnRequestApprovedEvent(Long returnRequestId, Long assetId, Long organizationId, Long returnedById, String comment, String approvedBy) {
        this(returnRequestId, assetId, organizationId, returnedById, comment, approvedBy, LocalDateTime.now());
    }
}
