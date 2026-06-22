package com.example.ems.common.dto.manager;

public record PerformanceDto(
    Integer attendance,
    Integer performance,
    Integer goalCompletion,
    Integer training
) {}
