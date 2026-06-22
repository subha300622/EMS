package com.example.ems.common.dto.manager;

import java.time.Instant;

public record DashboardMetadata(
    Instant generatedAt,
    Long managerId,
    Integer teamSize
) {}
