package com.example.ems.common.dto.manager;

import java.util.List;

public record ManagerDashboardResponse(
    DashboardMetadata metadata,
    SummaryDto summary,
    List<AttendanceTrendDto> attendanceTrend,
    TeamCompositionDto teamComposition,
    List<TeamMemberDto> recentTeamMembers,
    PerformanceDto performance,
    PendingApprovalCountsDto pendingApprovals,
    List<TeamTodayDto> teamToday,
    List<AlertDto> alerts,
    List<InsightDto> insights
) {}
