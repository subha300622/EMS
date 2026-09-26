package com.example.ems.superadmin.dashboard.controller;

import com.example.ems.superadmin.dashboard.dto.DashboardPeriod;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import com.example.ems.superadmin.dashboard.service.SuperAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/super-admin/dashboard")
@CrossOrigin("*")
@Tag(name = "Super Admin Dashboard", description = "Platform-level aggregated dashboard and drill-down metrics")
public class SuperAdminDashboardController {

    @Autowired
    private SuperAdminDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get aggregated platform super admin dashboard")
    public ResponseEntity<SuperAdminDashboardResponse> getDashboard(
            @RequestParam(name = "period", required = false, defaultValue = "MONTH") DashboardPeriod period,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        SuperAdminDashboardResponse response = dashboardService.getAggregatedDashboard(period, from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organizations")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get organization overview drill-down statistics")
    public ResponseEntity<OrganizationDrilldownStats> getOrganizations(
            @RequestParam(name = "period", required = false, defaultValue = "MONTH") DashboardPeriod period,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(dashboardService.getOrganizationStats(period, from, to));
    }

    @GetMapping("/employees")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get employee overview drill-down statistics")
    public ResponseEntity<EmployeeDrilldownStats> getEmployees(
            @RequestParam(name = "period", required = false, defaultValue = "MONTH") DashboardPeriod period,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(dashboardService.getEmployeeStats(period, from, to));
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get platform-wide subscription statistics")
    public ResponseEntity<SubscriptionStats> getSubscriptions() {
        return ResponseEntity.ok(dashboardService.getSubscriptionStats());
    }

    @GetMapping("/revenue")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get platform revenue metrics")
    public ResponseEntity<RevenueStats> getRevenue(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(dashboardService.getRevenueStats(from, to));
    }

    @GetMapping("/attendance")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get platform-wide attendance overview")
    public ResponseEntity<AttendanceStats> getAttendance(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(dashboardService.getAttendanceStats(from, to));
    }

    @GetMapping("/leave")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get platform-wide leave overview")
    public ResponseEntity<LeaveStats> getLeave(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(dashboardService.getLeaveStats(from, to));
    }

    @GetMapping("/payroll")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get platform-wide payroll overview")
    public ResponseEntity<PayrollStats> getPayroll() {
        return ResponseEntity.ok(dashboardService.getPayrollStats());
    }

    @GetMapping("/recent-organizations")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get recent organizations")
    public ResponseEntity<PageDto<RecentOrganizationDto>> getRecentOrganizations(
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {

        return ResponseEntity.ok(dashboardService.getRecentOrganizations(limit));
    }

    @GetMapping("/recent-users")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get recent users")
    public ResponseEntity<PageDto<RecentUserDto>> getRecentUsers(
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {

        return ResponseEntity.ok(dashboardService.getRecentUsers(limit));
    }

    @GetMapping("/activity")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get recent platform activities")
    public ResponseEntity<PageDto<RecentActivityDto>> getActivity(
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {

        return ResponseEntity.ok(dashboardService.getRecentActivities(limit));
    }

    @GetMapping("/charts/growth")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get growth chart data")
    public ResponseEntity<GrowthChartResponse> getGrowthChart(
            @RequestParam(name = "period", required = false, defaultValue = "YEAR") DashboardPeriod period) {

        return ResponseEntity.ok(dashboardService.getGrowthChart());
    }

    @GetMapping("/charts/revenue")
    @PreAuthorize("@permissionCheckService.hasPermission(T(com.example.ems.auth.service.PermissionRegistry).PLATFORM_DASHBOARD_VIEW)")
    @Operation(summary = "Get revenue trend chart data")
    public ResponseEntity<RevenueChartResponse> getRevenueChart() {
        return ResponseEntity.ok(dashboardService.getRevenueChart());
    }
}
