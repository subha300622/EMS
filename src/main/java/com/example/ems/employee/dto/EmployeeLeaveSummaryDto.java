package com.example.ems.employee.dto;

import java.util.List;

public record EmployeeLeaveSummaryDto(
    long availableLeaves,
    long usedLeaves,
    long pendingLeaves,
    List<UpcomingLeaveDto> upcomingLeaves
) {}
