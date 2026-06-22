package com.example.ems.common.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.common.dto.manager.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ManagerDashboardService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SummaryWidgetService summaryWidgetService;

    @Autowired
    private AttendanceWidgetService attendanceWidgetService;

    @Autowired
    private TeamWidgetService teamWidgetService;

    @Autowired
    private ApprovalWidgetService approvalWidgetService;

    @Autowired
    private NotificationWidgetService notificationWidgetService;

    public List<Employee> getTeam(Employee manager) {
        if (manager == null) {
            return Collections.emptyList();
        }
        return employeeRepository.findByManagerId(manager.getId());
    }

    public SummaryDto getSummary(Employee manager) {
        return summaryWidgetService.getSummary(manager, getTeam(manager));
    }

    @Cacheable(value = "manager-attendance", key = "#manager.id + '-' + #period")
    public List<AttendanceTrendDto> getAttendanceTrend(Employee manager, DashboardPeriod period) {
        return attendanceWidgetService.getAttendanceTrend(manager, getTeam(manager), period);
    }

    public TeamCompositionDto getTeamComposition(Employee manager) {
        return teamWidgetService.getTeamComposition(manager, getTeam(manager));
    }

    public Page<TeamMemberDto> getTeamMembers(Employee manager, int page, int size) {
        return teamWidgetService.getTeamMembers(manager, getTeam(manager), page, size);
    }

    public PerformanceDto getPerformance(Employee manager) {
        if (getTeam(manager).isEmpty() && !isDevProfile()) {
            return new PerformanceDto(0, 0, 0, 0);
        }
        return new PerformanceDto(92, 86, 79, 95);
    }

    public PendingApprovalCountsDto getApprovalSummary(Employee manager) {
        return approvalWidgetService.getApprovalSummary(manager, getTeam(manager));
    }

    public List<PendingApprovalDto> getPendingApprovals(Employee manager) {
        return approvalWidgetService.getPendingApprovals(manager, getTeam(manager));
    }

    public List<TeamTodayDto> getTeamToday(Employee manager) {
        return teamWidgetService.getTeamToday(manager, getTeam(manager));
    }

    public List<AlertDto> getAlerts(Employee manager) {
        return notificationWidgetService.getAlerts(manager, getTeam(manager));
    }

    public List<QuickActionDto> getQuickActions(Employee manager) {
        return notificationWidgetService.getQuickActions(manager, getTeam(manager));
    }

    @Cacheable(value = "manager-notifications", key = "#manager.id")
    public List<NotificationDto> getNotifications(Employee manager) {
        return notificationWidgetService.getNotifications(manager, getTeam(manager));
    }

    public ScheduleSnapshotDto getScheduleSnapshot(Employee manager) {
        return summaryWidgetService.getScheduleSnapshot(manager, getTeam(manager));
    }

    public List<InsightDto> getInsights(Employee manager) {
        return notificationWidgetService.getInsights(manager, getTeam(manager));
    }

    public List<OvertimeDto> getOvertime(Employee manager) {
        return attendanceWidgetService.getOvertime(manager, getTeam(manager));
    }

    public List<LeaveSummaryDto> getLeaveSummary(Employee manager) {
        return approvalWidgetService.getLeaveSummary(manager, getTeam(manager));
    }

    public List<UpcomingEventDto> getUpcomingEvents(Employee manager) {
        return teamWidgetService.getUpcomingEvents(manager, getTeam(manager));
    }

    @Cacheable(value = "manager-dashboard", key = "#manager.id")
    public ManagerDashboardResponse getAggregatedDashboard(Employee manager, Set<String> widgets) {
        List<Employee> team = getTeam(manager);

        SummaryDto summary = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("summary")) {
            summary = summaryWidgetService.getSummary(manager, team);
        }

        List<AttendanceTrendDto> attendanceTrend = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("attendanceTrend") || widgets.contains("attendance")) {
            attendanceTrend = attendanceWidgetService.getAttendanceTrend(manager, team, DashboardPeriod.MONTH);
        }

        TeamCompositionDto teamComposition = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("teamComposition")) {
            teamComposition = teamWidgetService.getTeamComposition(manager, team);
        }

        List<TeamMemberDto> recentTeamMembers = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("teamMembers")) {
            recentTeamMembers = teamWidgetService.getTeamMembers(manager, team, 0, 10).getContent();
        }

        PerformanceDto performance = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("performance")) {
            performance = getPerformance(manager);
        }

        PendingApprovalCountsDto pendingApprovals = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("pendingApprovals")) {
            pendingApprovals = approvalWidgetService.getApprovalSummary(manager, team);
        }

        List<TeamTodayDto> teamToday = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("teamToday")) {
            teamToday = teamWidgetService.getTeamToday(manager, team);
        }

        List<AlertDto> alerts = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("alerts")) {
            alerts = notificationWidgetService.getAlerts(manager, team);
        }

        List<InsightDto> insights = null;
        if (widgets == null || widgets.isEmpty() || widgets.contains("insights")) {
            insights = notificationWidgetService.getInsights(manager, team);
        }

        DashboardMetadata metadata = new DashboardMetadata(
                Instant.now(),
                manager.getId(),
                team.size()
        );

        return new ManagerDashboardResponse(
                metadata,
                summary,
                attendanceTrend,
                teamComposition,
                recentTeamMembers,
                performance,
                pendingApprovals,
                teamToday,
                alerts,
                insights
        );
    }

    @Caching(evict = {
        @CacheEvict(value = "manager-dashboard", key = "#manager.id"),
        @CacheEvict(value = "manager-attendance", allEntries = true),
        @CacheEvict(value = "manager-notifications", key = "#manager.id")
    })
    public void evictDashboardCache(Employee manager) {
        // Intentionally empty. CacheEvict handles eviction automatically.
    }

    @Autowired
    private org.springframework.core.env.Environment environment;

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }
}
