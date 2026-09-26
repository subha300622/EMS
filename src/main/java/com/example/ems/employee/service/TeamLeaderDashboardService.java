package com.example.ems.employee.service;

import com.example.ems.employee.dto.teamleader.*;

import java.util.List;

public interface TeamLeaderDashboardService {

    TeamLeaderDashboardResponse getDashboard();

    List<TeamMemberRosterDto> getTeamMembers();

    TeamMemberRosterDto getTeamMemberById(Long memberEmployeeId);

    TeamLeaderAttendanceTodayResponse getAttendanceToday();

    TeamLeaderPerformanceResponse getPerformance();

    void recommendLeave(Long leaveId, TeamLeaveRecommendationRequest request);
}
