package com.example.ems.asset.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssetDisposedEvent(
    Long assetId,
    Long organizationId,
    String disposalMethod,
    BigDecimal disposalValue,
    String remarks,
    String performedBy,
    LocalDateTime timestamp
) {
    public AssetDisposedEvent(Long assetId, Long organizationId, String disposalMethod, BigDecimal disposalValue, String remarks, String performedBy) {
        this(assetId, organizationId, disposalMethod, disposalValue, remarks, performedBy, LocalDateTime.now());
    }
}
