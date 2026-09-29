package com.example.ems.asset.event;

import java.time.LocalDateTime;

public record AssetRequestApprovedEvent(
    Long requestId,
    Long assetId,
    Long organizationId,
    Long requestedById,
    String comment,
    String approvedBy,
    LocalDateTime timestamp
) {
    public AssetRequestApprovedEvent(Long requestId, Long assetId, Long organizationId, Long requestedById, String comment, String approvedBy) {
        this(requestId, assetId, organizationId, requestedById, comment, approvedBy, LocalDateTime.now());
    }
}
