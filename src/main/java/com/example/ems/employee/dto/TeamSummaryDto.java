package com.example.ems.employee.dto;

public record TeamSummaryDto(
    int teamSize,
    int active,
    int wfh,
    int onLeave
) {}
