package com.example.ems.common.service;

import com.example.ems.config.BaseCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Cache facade for the HR Dashboard endpoints.
 */
@Service
public class HrDashboardCacheService extends BaseCacheService {

    private static final String PREFIX = "ems:%s:hr-dashboard:v1:";
    
    @Autowired
    private HrDashboardService hrDashboardService;

    // ── Key builders ─────────────────────────────────────────────────────────
    private String keySummary() { return String.format(PREFIX + "summary", env); }
    private String keyHeadcount() { return String.format(PREFIX + "headcount", env); }
    private String keyNewHires() { return String.format(PREFIX + "new_hires", env); }
    private String keyAttrition() { return String.format(PREFIX + "attrition", env); }
    private String keyOpenPositions() { return String.format(PREFIX + "open_positions", env); }
    private String keyTrend(String period) { return String.format(PREFIX + "trend:%s", env, period); }
    private String keyBreakdown() { return String.format(PREFIX + "breakdown", env); }
    private String keyPendingLeaves() { return String.format(PREFIX + "pending_leaves", env); }
    private String keyRecentHires() { return String.format(PREFIX + "recent_hires", env); }
    private String keySearch(String keyword) { return String.format(PREFIX + "search:%s", env, keyword); }
    private String keyAggregation() { return String.format(PREFIX + "aggregation", env); }
    private String keyAttendanceByDepartment() { return String.format(PREFIX + "attendance_by_department", env); }
    private String keyRetentionAlerts() { return String.format(PREFIX + "retention_alerts", env); }

    // ── Cached GETs ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDashboardSummary() {
        return get(keySummary(), CacheCategory.DASHBOARD, Map.class,
                () -> hrDashboardService.getDashboardSummary());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getHeadcountStats() {
        return get(keyHeadcount(), CacheCategory.DASHBOARD, Map.class,
                () -> hrDashboardService.getHeadcountStats());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getNewHiresStats() {
        return get(keyNewHires(), CacheCategory.DASHBOARD, Map.class,
                () -> hrDashboardService.getNewHiresStats());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAttritionStats() {
        return get(keyAttrition(), CacheCategory.DASHBOARD, Map.class,
                () -> hrDashboardService.getAttritionStats());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getOpenPositionsStats() {
        return get(keyOpenPositions(), CacheCategory.DASHBOARD, Map.class,
                () -> hrDashboardService.getOpenPositionsStats());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getHeadcountTrend(String period) {
        return get(keyTrend(period), CacheCategory.DASHBOARD_CHART, Map.class,
                () -> hrDashboardService.getHeadcountTrend(period));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getEmployeeBreakdown() {
        return get(keyBreakdown(), CacheCategory.DASHBOARD_CHART, Map.class,
                () -> hrDashboardService.getEmployeeBreakdown());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPendingLeaves() {
        return get(keyPendingLeaves(), CacheCategory.DASHBOARD, List.class,
                () -> hrDashboardService.getPendingLeaves());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getRecentHires() {
        return get(keyRecentHires(), CacheCategory.DASHBOARD, List.class,
                () -> hrDashboardService.getRecentHires());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> globalSearch(String keyword) {
        // search has a very short TTL, handled as DEFAULT (5 min) or we can specify REFERENCE_DATA
        return get(keySearch(keyword), CacheCategory.DEFAULT, Map.class,
                () -> hrDashboardService.globalSearch(keyword));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDashboardSummaryAggregation() {
        return get(keyAggregation(), CacheCategory.DASHBOARD, Map.class,
                () -> hrDashboardService.getDashboardSummaryAggregation());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAttendanceByDepartment() {
        return get(keyAttendanceByDepartment(), CacheCategory.DASHBOARD, List.class,
                () -> hrDashboardService.getAttendanceByDepartment());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getRetentionAlerts() {
        return get(keyRetentionAlerts(), CacheCategory.DASHBOARD, List.class,
                () -> hrDashboardService.getRetentionAlerts());
    }

    // ── Eviction ─────────────────────────────────────────────────────────────

    /**
     * Evicts all cached HR Dashboard entries (L1 + L2).
     * Call this when data changes (e.g. employee joined, leave request submitted, etc.).
     */
    public void evictAllDashboard() {
        log.info("[Cache] Evicting all HR Dashboard caches");
        evict(keySummary(), CacheCategory.DASHBOARD);
        evict(keyHeadcount(), CacheCategory.DASHBOARD);
        evict(keyNewHires(), CacheCategory.DASHBOARD);
        evict(keyAttrition(), CacheCategory.DASHBOARD);
        evict(keyOpenPositions(), CacheCategory.DASHBOARD);
        evict(keyBreakdown(), CacheCategory.DASHBOARD_CHART);
        evict(keyPendingLeaves(), CacheCategory.DASHBOARD);
        evict(keyRecentHires(), CacheCategory.DASHBOARD);
        evict(keyAggregation(), CacheCategory.DASHBOARD);
        evict(keyAttendanceByDepartment(), CacheCategory.DASHBOARD);
        evict(keyRetentionAlerts(), CacheCategory.DASHBOARD);
        evict(keyTrend("3months"), CacheCategory.DASHBOARD_CHART);
        evict(keyTrend("6months"), CacheCategory.DASHBOARD_CHART);
        evict(keyTrend("12months"), CacheCategory.DASHBOARD_CHART);
    }
}
