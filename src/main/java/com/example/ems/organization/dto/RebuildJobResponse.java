package com.example.ems.organization.dto;

public record RebuildJobResponse(
    String rebuildId,
    String status,
    String mode,
    long estimatedDurationMs
) {}
