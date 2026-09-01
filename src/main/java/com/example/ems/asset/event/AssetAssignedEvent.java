package com.example.ems.asset.event;

import java.time.LocalDateTime;

public record AssetAssignedEvent(
    Long assetId,
    Long organizationId,
    Long assignedToId,
    String assignedToType,
    String performedBy,
    LocalDateTime timestamp
) {
    public AssetAssignedEvent(Long assetId, Long organizationId, Long assignedToId, String assignedToType, String performedBy) {
        this(assetId, organizationId, assignedToId, assignedToType, performedBy, LocalDateTime.now());
    }
}
