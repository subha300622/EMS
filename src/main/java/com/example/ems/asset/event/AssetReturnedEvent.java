package com.example.ems.asset.event;

import java.time.LocalDateTime;

public record AssetReturnedEvent(
    Long assetId,
    Long organizationId,
    Long returnedById,
    String condition,
    String remarks,
    String performedBy,
    LocalDateTime timestamp
) {
    public AssetReturnedEvent(Long assetId, Long organizationId, Long returnedById, String condition, String remarks, String performedBy) {
        this(assetId, organizationId, returnedById, condition, remarks, performedBy, LocalDateTime.now());
    }
}
