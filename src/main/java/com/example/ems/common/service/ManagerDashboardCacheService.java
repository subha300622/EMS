package com.example.ems.common.service;

import com.example.ems.config.BaseCacheService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.common.dto.manager.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Cache facade for the Manager Dashboard endpoints.
 */
@Service
public class ManagerDashboardCacheService extends BaseCacheService {

    private static final String PREFIX = "ems:%s:mgr-dashboard:v1:";

    @Autowired
    private ManagerDashboardService managerDashboardService;

    // ── Key builders ─────────────────────────────────────────────────────────
    private String keyAgg(Long managerId, Set<String> widgets) {
        String widgetStr = widgets == null || widgets.isEmpty() ? "all" : String.join("-", widgets);
        return String.format(PREFIX + "agg:%d:%s", env, managerId, widgetStr);
    }
    private String keySummary(Long managerId) { return String.format(PREFIX + "summary:%d", env, managerId); }
    private String keyAttendance(Long managerId, String period) { return String.format(PREFIX + "attendance:%d:%s", env, managerId, period); }
    private String keyComposition(Long managerId) { return String.format(PREFIX + "composition:%d", env, managerId); }
    private String keyMembers(Long managerId, int page, int size) { return String.format(PREFIX + "members:%d:%d:%d", env, managerId, page, size); }
    private String keyPerformance(Long managerId) { return String.format(PREFIX + "performance:%d", env, managerId); }
    private String keyOvertime(Long managerId) { return String.format(PREFIX + "overtime:%d", env, managerId); }
    private String keyPendingApprovals(Long managerId) { return String.format(PREFIX + "pending_approvals:%d", env, managerId); }
    private String keyApprovalSummary(Long managerId) { return String.format(PREFIX + "approval_summary:%d", env, managerId); }
    private String keyToday(Long managerId) { return String.format(PREFIX + "today:%d", env, managerId); }
    private String keyLeaveSummary(Long managerId) { return String.format(PREFIX + "leave_summary:%d", env, managerId); }
    private String keyUpcomingEvents(Long managerId) { return String.format(PREFIX + "upcoming_events:%d", env, managerId); }
    private String keyAlerts(Long managerId) { return String.format(PREFIX + "alerts:%d", env, managerId); }
    private String keyInsights(Long managerId) { return String.format(PREFIX + "insights:%d", env, managerId); }
    private String keyActions(Long managerId) { return String.format(PREFIX + "actions:%d", env, managerId); }
    private String keyNotifications(Long managerId) { return String.format(PREFIX + "notifications:%d", env, managerId); }
    private String keySchedule(Long managerId) { return String.format(PREFIX + "schedule:%d", env, managerId); }

    // ── Cached GETs ──────────────────────────────────────────────────────────

    public ManagerDashboardResponse getAggregatedDashboard(Employee manager, Set<String> widgets) {
        if (manager == null) return null;
        return get(keyAgg(manager.getId(), widgets), CacheCategory.DASHBOARD, ManagerDashboardResponse.class,
                () -> managerDashboardService.getAggregatedDashboard(manager, widgets));
    }

    public SummaryDto getSummary(Employee manager) {
        if (manager == null) return null;
        return get(keySummary(manager.getId()), CacheCategory.DASHBOARD, SummaryDto.class,
                () -> managerDashboardService.getSummary(manager));
    }

    @SuppressWarnings("unchecked")
    public List<AttendanceTrendDto> getAttendanceTrend(Employee manager, DashboardPeriod period) {
        if (manager == null) return null;
        String pStr = period != null ? period.name() : "MONTH";
        return (List<AttendanceTrendDto>) get(keyAttendance(manager.getId(), pStr), CacheCategory.DASHBOARD_CHART, List.class,
                () -> managerDashboardService.getAttendanceTrend(manager, period));
    }

    public TeamCompositionDto getTeamComposition(Employee manager) {
        if (manager == null) return null;
        return get(keyComposition(manager.getId()), CacheCategory.DASHBOARD_CHART, TeamCompositionDto.class,
                () -> managerDashboardService.getTeamComposition(manager));
    }

    @SuppressWarnings("unchecked")
    public Page<TeamMemberDto> getTeamMembers(Employee manager, int page, int size) {
        if (manager == null) return null;
        // Paginated results handled as LIST category
        return (Page<TeamMemberDto>) get(keyMembers(manager.getId(), page, size), CacheCategory.LIST, Page.class,
                () -> managerDashboardService.getTeamMembers(manager, page, size));
    }

    public PerformanceDto getPerformance(Employee manager) {
        if (manager == null) return null;
        return get(keyPerformance(manager.getId()), CacheCategory.DASHBOARD_CHART, PerformanceDto.class,
                () -> managerDashboardService.getPerformance(manager));
    }

    @SuppressWarnings("unchecked")
    public List<OvertimeDto> getOvertime(Employee manager) {
        if (manager == null) return null;
        return (List<OvertimeDto>) get(keyOvertime(manager.getId()), CacheCategory.DASHBOARD_CHART, List.class,
                () -> managerDashboardService.getOvertime(manager));
    }

    @SuppressWarnings("unchecked")
    public List<PendingApprovalDto> getPendingApprovals(Employee manager) {
        if (manager == null) return null;
        return (List<PendingApprovalDto>) get(keyPendingApprovals(manager.getId()), CacheCategory.APPROVAL_QUEUE, List.class,
                () -> managerDashboardService.getPendingApprovals(manager));
    }

    public PendingApprovalCountsDto getApprovalSummary(Employee manager) {
        if (manager == null) return null;
        return get(keyApprovalSummary(manager.getId()), CacheCategory.APPROVAL_QUEUE, PendingApprovalCountsDto.class,
                () -> managerDashboardService.getApprovalSummary(manager));
    }

    @SuppressWarnings("unchecked")
    public List<TeamTodayDto> getTeamToday(Employee manager) {
        if (manager == null) return null;
        return (List<TeamTodayDto>) get(keyToday(manager.getId()), CacheCategory.DASHBOARD, List.class,
                () -> managerDashboardService.getTeamToday(manager));
    }

    @SuppressWarnings("unchecked")
    public List<LeaveSummaryDto> getLeaveSummary(Employee manager) {
        if (manager == null) return null;
        return (List<LeaveSummaryDto>) get(keyLeaveSummary(manager.getId()), CacheCategory.DASHBOARD, List.class,
                () -> managerDashboardService.getLeaveSummary(manager));
    }

    @SuppressWarnings("unchecked")
    public List<UpcomingEventDto> getUpcomingEvents(Employee manager) {
        if (manager == null) return null;
        return (List<UpcomingEventDto>) get(keyUpcomingEvents(manager.getId()), CacheCategory.DASHBOARD, List.class,
                () -> managerDashboardService.getUpcomingEvents(manager));
    }

    @SuppressWarnings("unchecked")
    public List<AlertDto> getAlerts(Employee manager) {
        if (manager == null) return null;
        return (List<AlertDto>) get(keyAlerts(manager.getId()), CacheCategory.DASHBOARD, List.class,
                () -> managerDashboardService.getAlerts(manager));
    }

    @SuppressWarnings("unchecked")
    public List<InsightDto> getInsights(Employee manager) {
        if (manager == null) return null;
        return (List<InsightDto>) get(keyInsights(manager.getId()), CacheCategory.DASHBOARD, List.class,
                () -> managerDashboardService.getInsights(manager));
    }

    @SuppressWarnings("unchecked")
    public List<QuickActionDto> getQuickActions(Employee manager) {
        if (manager == null) return null;
        return (List<QuickActionDto>) get(keyActions(manager.getId()), CacheCategory.DASHBOARD, List.class,
                () -> managerDashboardService.getQuickActions(manager));
    }

    @SuppressWarnings("unchecked")
    public List<NotificationDto> getNotifications(Employee manager) {
        if (manager == null) return null;
        return (List<NotificationDto>) get(keyNotifications(manager.getId()), CacheCategory.DASHBOARD, List.class,
                () -> managerDashboardService.getNotifications(manager));
    }

    public ScheduleSnapshotDto getScheduleSnapshot(Employee manager) {
        if (manager == null) return null;
        return get(keySchedule(manager.getId()), CacheCategory.DASHBOARD, ScheduleSnapshotDto.class,
                () -> managerDashboardService.getScheduleSnapshot(manager));
    }

    // ── Eviction ─────────────────────────────────────────────────────────────

    /**
     * Evicts all dashboard cache entries for a manager.
     */
    public void evictDashboardCache(Employee manager) {
        if (manager == null) return;
        Long mid = manager.getId();
        evict(keySummary(mid), CacheCategory.DASHBOARD);
        evict(keyComposition(mid), CacheCategory.DASHBOARD_CHART);
        evict(keyPerformance(mid), CacheCategory.DASHBOARD_CHART);
        evict(keyOvertime(mid), CacheCategory.DASHBOARD_CHART);
        evict(keyPendingApprovals(mid), CacheCategory.APPROVAL_QUEUE);
        evict(keyApprovalSummary(mid), CacheCategory.APPROVAL_QUEUE);
        evict(keyToday(mid), CacheCategory.DASHBOARD);
        evict(keyLeaveSummary(mid), CacheCategory.DASHBOARD);
        evict(keyUpcomingEvents(mid), CacheCategory.DASHBOARD);
        evict(keyAlerts(mid), CacheCategory.DASHBOARD);
        evict(keyInsights(mid), CacheCategory.DASHBOARD);
        evict(keyActions(mid), CacheCategory.DASHBOARD);
        evict(keyNotifications(mid), CacheCategory.DASHBOARD);
        evict(keySchedule(mid), CacheCategory.DASHBOARD);

        // Evict specific attendance trend configurations
        for (DashboardPeriod period : DashboardPeriod.values()) {
            evict(keyAttendance(mid, period.name()), CacheCategory.DASHBOARD_CHART);
        }

        // Evict paginated member lists (default page ranges/sizes or clear all)
        evict(keyMembers(mid, 0, 10), CacheCategory.LIST);
        
        // Evict aggregated dashboards (with common widget requests or clean all using wildcards)
        evict(keyAgg(mid, null), CacheCategory.DASHBOARD);
    }
}
