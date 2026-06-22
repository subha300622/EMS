package com.example.ems.common.dto.manager;

public record ScheduleSnapshotDto(
    Long todayShifts,
    Long dayShift,
    Long nightShift,
    Long wfh
) {}
