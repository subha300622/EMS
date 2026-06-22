package com.example.ems.common.dto.manager;

public record InsightDto(
    InsightSeverity severity,
    String message
) {}
