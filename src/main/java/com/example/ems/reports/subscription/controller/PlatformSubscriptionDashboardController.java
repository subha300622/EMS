package com.example.ems.reports.subscription.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.reports.subscription.facade.SubscriptionDashboardFacade;
import com.example.ems.reports.subscription.dto.*;
import com.example.ems.security.service.JwtService;
import com.example.ems.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/platform/reports/subscriptions", "/api/v1/platform/reports/subscriptions"})
@CrossOrigin("*")
@Tag(name = "Platform Subscription Dashboard", description = "High-level subscription dashboard and analytics for platform admins")
public class PlatformSubscriptionDashboardController {

    @Autowired
    private SubscriptionDashboardFacade dashboardFacade;

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
            auditLogService.logAction(
                    user.getEmployeeId(), 
                    user.getWorkEmail(), 
                    action, 
                    "Subscription Dashboard", 
                    "DASHBOARD", 
                    "127.0.0.1", 
                    details
            );
        }
    }

    @Operation(summary = "Get high-level subscription statistics summary")
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        SubscriptionDashboardSummary data = dashboardFacade.getSummary();
        logAuditEvent(authHeader, "VIEW", "Subscription dashboard summary viewed");
        return ResponseEntity.ok(ApiResponse.success("Subscription summary loaded successfully", data));
    }

    @Operation(summary = "Get subscription growth trends")
    @GetMapping("/growth")
    public ResponseEntity<?> getGrowth(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        List<SubscriptionGrowthEntry> data = dashboardFacade.getGrowth(period, from, to);
        logAuditEvent(authHeader, "VIEW", "Subscription growth trend viewed for period: " + period);
        return ResponseEntity.ok(ApiResponse.success("Growth trend loaded successfully", data));
    }

    @Operation(summary = "Get subscription status distribution")
    @GetMapping("/status")
    public ResponseEntity<?> getStatusDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        SubscriptionStatusResponse data = dashboardFacade.getStatusDistribution();
        logAuditEvent(authHeader, "VIEW", "Subscription status distribution viewed");
        return ResponseEntity.ok(ApiResponse.success("Status distribution loaded successfully", data));
    }

    @Operation(summary = "Get revenue trends report")
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        List<RevenueReportEntry> data = dashboardFacade.getRevenueReport(period, from, to);
        logAuditEvent(authHeader, "VIEW", "Subscription revenue report viewed for period: " + period);
        return ResponseEntity.ok(ApiResponse.success("Revenue report loaded successfully", data));
    }

    @Operation(summary = "Get monthly revenue by subscription plan")
    @GetMapping("/plans")
    public ResponseEntity<?> getPlanRevenue(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        List<PlanRevenueEntry> data = dashboardFacade.getPlanRevenue();
        logAuditEvent(authHeader, "VIEW", "Subscription plan revenue distribution viewed");
        return ResponseEntity.ok(ApiResponse.success("Plan revenue loaded successfully", data));
    }

    @Operation(summary = "Get plan distribution percentages")
    @GetMapping("/distribution")
    public ResponseEntity<?> getPlanDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        List<PlanDistributionEntry> data = dashboardFacade.getPlanDistribution();
        logAuditEvent(authHeader, "VIEW", "Subscription plan distribution percentages viewed");
        return ResponseEntity.ok(ApiResponse.success("Plan distribution loaded successfully", data));
    }

    @Operation(summary = "Get trial to paid conversion rates")
    @GetMapping("/conversion")
    public ResponseEntity<?> getConversion(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        SubscriptionConversionResponse data = dashboardFacade.getConversion();
        logAuditEvent(authHeader, "VIEW", "Subscription trial conversion analytics viewed");
        return ResponseEntity.ok(ApiResponse.success("Conversion analytics loaded successfully", data));
    }

    @Operation(summary = "Get churn and retention metrics")
    @GetMapping("/churn")
    public ResponseEntity<?> getChurn(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        SubscriptionChurnResponse data = dashboardFacade.getChurn();
        logAuditEvent(authHeader, "VIEW", "Subscription churn metrics viewed");
        return ResponseEntity.ok(ApiResponse.success("Churn metrics loaded successfully", data));
    }
}
