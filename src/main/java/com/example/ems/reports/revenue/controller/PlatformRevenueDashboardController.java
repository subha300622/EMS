package com.example.ems.reports.revenue.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.reports.revenue.facade.RevenueDashboardFacade;
import com.example.ems.reports.revenue.dto.*;
import com.example.ems.reports.revenue.validator.RevenueReportValidator;
import com.example.ems.security.service.JwtService;
import com.example.ems.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform/revenue/dashboard")
@CrossOrigin("*")
@Tag(name = "Platform Revenue Dashboard", description = "Revenue dashboard analytics for platform admins")
public class PlatformRevenueDashboardController {

    @Autowired
    private RevenueDashboardFacade dashboardFacade;

    @Autowired
    private RevenueReportValidator reportValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditLogService auditLogService;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    private ResponseEntity<?> validateAccess(String authHeader, String requiredPermission) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(user.getWorkEmail(), requiredPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires permission " + requiredPermission, "AUTH_002"));
        }
        return null;
    }

    private void logAuditEvent(String authHeader, String action, String details) {
        User user = resolveUser(authHeader);
        if (user != null) {
            String clientIp = "0.0.0.0";
            try {
                org.springframework.web.context.request.ServletRequestAttributes attrs =
                        (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    clientIp = com.example.ems.common.util.ClientIpResolver.getClientIp(attrs.getRequest());
                }
            } catch (Exception ignored) {}
            auditLogService.logAction(
                    user.getEmployeeId(), 
                    user.getWorkEmail(), 
                    action, 
                    "Revenue Dashboard", 
                    "REVENUE", 
                    clientIp, 
                    details
            );
        }
    }

    @Operation(summary = "Get overall revenue dashboard (aggregated overview)")
    @GetMapping
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_DASHBOARD_VIEW);
        if (accessCheck != null) return accessCheck;

        dashboardFacade.refreshMaterializedViews();
        RevenueSummaryResponse summary = dashboardFacade.getSummary();
        List<RevenueTrendResponse> trends = dashboardFacade.getTrends();
        List<RevenueGrowthResponse> growth = dashboardFacade.getGrowth();
        RevenueForecastResponse forecast = dashboardFacade.getForecast(6);

        RevenueDashboardResponse dashboard = new RevenueDashboardResponse(summary, trends, growth, forecast);
        logAuditEvent(authHeader, "REVENUE_DASHBOARD_VIEWED", "Revenue Dashboard Viewed");

        return ResponseEntity.ok(ApiResponse.success("Revenue dashboard metrics loaded successfully", dashboard));
    }

    @Operation(summary = "Get revenue summary cards")
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_DASHBOARD_VIEW);
        if (accessCheck != null) return accessCheck;

        dashboardFacade.refreshMaterializedViews();
        RevenueSummaryResponse data = dashboardFacade.getSummary();
        logAuditEvent(authHeader, "REVENUE_SUMMARY_VIEWED", "Revenue Summary Cards Viewed");
        return ResponseEntity.ok(ApiResponse.success("Revenue summary cards loaded successfully", data));
    }

    @Operation(summary = "Get revenue KPIs (alias for summary)")
    @GetMapping("/kpis")
    public ResponseEntity<?> getKpis(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_DASHBOARD_VIEW);
        if (accessCheck != null) return accessCheck;

        dashboardFacade.refreshMaterializedViews();
        RevenueSummaryResponse data = dashboardFacade.getSummary();
        logAuditEvent(authHeader, "REVENUE_KPIS_VIEWED", "Revenue KPIs Viewed");
        return ResponseEntity.ok(ApiResponse.success("Revenue KPIs loaded successfully", data));
    }

    @Operation(summary = "Get monthly revenue trends")
    @GetMapping("/trends")
    public ResponseEntity<?> getTrends(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_DASHBOARD_VIEW);
        if (accessCheck != null) return accessCheck;

        dashboardFacade.refreshMaterializedViews();
        List<RevenueTrendResponse> data = dashboardFacade.getTrends();
        logAuditEvent(authHeader, "REVENUE_TRENDS_VIEWED", "Revenue Trends Viewed");
        return ResponseEntity.ok(ApiResponse.success("Revenue trends loaded successfully", data));
    }

    @Operation(summary = "Get revenue growth statistics")
    @GetMapping("/growth")
    public ResponseEntity<?> getGrowth(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_DASHBOARD_VIEW);
        if (accessCheck != null) return accessCheck;

        dashboardFacade.refreshMaterializedViews();
        List<RevenueGrowthResponse> data = dashboardFacade.getGrowth();
        logAuditEvent(authHeader, "REVENUE_GROWTH_VIEWED", "Revenue Growth Viewed");
        return ResponseEntity.ok(ApiResponse.success("Revenue growth statistics loaded successfully", data));
    }

    @Operation(summary = "Get revenue forecast projections")
    @GetMapping("/forecast")
    public ResponseEntity<?> getForecast(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "6") int horizon) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_DASHBOARD_VIEW);
        if (accessCheck != null) return accessCheck;

        reportValidator.validateHorizon(horizon);
        RevenueForecastResponse data = dashboardFacade.getForecast(horizon);
        logAuditEvent(authHeader, "REVENUE_FORECAST_VIEWED", "Revenue Forecast Viewed for horizon: " + horizon);
        return ResponseEntity.ok(ApiResponse.success("Revenue forecast loaded successfully", data));
    }
}
