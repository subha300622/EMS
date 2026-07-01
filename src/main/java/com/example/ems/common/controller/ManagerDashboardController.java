package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.dto.manager.*;
import com.example.ems.common.service.ManagerDashboardCacheService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/manager/dashboard")
@CrossOrigin("*")
@Tag(name = "Manager Dashboard", description = "Manager-focused analytics and operational dashboard. Provides team attendance, composition, approvals, overtime, notifications, and team insights. Access: MANAGER, HR, ADMIN, SUPER_ADMIN")
public class ManagerDashboardController {

    @Autowired
    private ManagerDashboardCacheService managerDashboardCacheService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

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

    private boolean hasAccess(User user) {
        if (user == null) return false;
        return user.getRole() != null && (
                "MANAGER".equalsIgnoreCase(user.getRole().getName())
                || "ADMIN".equalsIgnoreCase(user.getRole().getName())
                || "SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName())
                || "HR".equalsIgnoreCase(user.getRole().getName())
        );
    }

    private Employee resolveEmployee(User user) {
        if (user == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }


    @Operation(summary = "Get Consolidated Aggregated Manager Dashboard (Single Call)")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<ManagerDashboardResponse>> getAggregatedDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "widgets", required = false) Set<String> widgets) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Aggregated dashboard summary retrieved", 
                managerDashboardCacheService.getAggregatedDashboard(emp, widgets)));
    }

    @Operation(summary = "Clear Dashboard Cache and Reload Metrics")
    @PostMapping("/refresh")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<ManagerDashboardResponse>> refreshDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        managerDashboardCacheService.evictDashboardCache(emp);
        return ResponseEntity.ok(ApiResponse.success("Dashboard cache refreshed successfully", 
                managerDashboardCacheService.getAggregatedDashboard(emp, null)));
    }

    @Operation(summary = "Get Dashboard Top KPI Cards Summary")
    @GetMapping("/summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<SummaryDto>> getSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Summary retrieved", managerDashboardCacheService.getSummary(emp)));
    }

    @Operation(summary = "Get Team Attendance Trend")
    @GetMapping("/attendance-trend")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<AttendanceTrendDto>>> getAttendanceTrend(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "period", defaultValue = "MONTH") DashboardPeriod period) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Attendance trend retrieved", managerDashboardCacheService.getAttendanceTrend(emp, period)));
    }

    @Operation(summary = "Get Team Composition Statistics")
    @GetMapping("/team-composition")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<TeamCompositionDto>> getTeamComposition(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Team composition retrieved", managerDashboardCacheService.getTeamComposition(emp)));
    }

    @Operation(summary = "Get Paginated Team Members Overview")
    @GetMapping("/team-members")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Page<TeamMemberDto>>> getTeamMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Team members retrieved", managerDashboardCacheService.getTeamMembers(emp, page, size)));
    }

    @Operation(summary = "Get Team Performance Indicators")
    @GetMapping("/performance")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<PerformanceDto>> getPerformance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Performance metrics retrieved", managerDashboardCacheService.getPerformance(emp)));
    }

    @Operation(summary = "Get Team Overtime Analytics")
    @GetMapping("/overtime")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<OvertimeDto>>> getOvertime(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Overtime metrics retrieved", managerDashboardCacheService.getOvertime(emp)));
    }

    @Operation(summary = "Get Team Pending Approvals Details")
    @GetMapping("/pending-approvals")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<PendingApprovalDto>>> getPendingApprovals(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Pending approvals retrieved", managerDashboardCacheService.getPendingApprovals(emp)));
    }

    @Operation(summary = "Get Team Approval Summary Counts")
    @GetMapping("/approval-summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<PendingApprovalCountsDto>> getApprovalSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Approval summary counts retrieved", managerDashboardCacheService.getApprovalSummary(emp)));
    }

    @Operation(summary = "Get Team Workforce Today Status")
    @GetMapping("/team-today")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<TeamTodayDto>>> getTeamToday(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Team today retrieved", managerDashboardCacheService.getTeamToday(emp)));
    }

    @Operation(summary = "Get Team Leaves Summary")
    @GetMapping("/leave-summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<LeaveSummaryDto>>> getLeaveSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave summary retrieved", managerDashboardCacheService.getLeaveSummary(emp)));
    }

    @Operation(summary = "Get Team Upcoming Events")
    @GetMapping("/upcoming-events")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<UpcomingEventDto>>> getUpcomingEvents(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Upcoming events retrieved", managerDashboardCacheService.getUpcomingEvents(emp)));
    }

    @Operation(summary = "Get Active Team Alerts")
    @GetMapping("/alerts")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<AlertDto>>> getAlerts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved", managerDashboardCacheService.getAlerts(emp)));
    }

    @Operation(summary = "Get Actionable Team Insights")
    @GetMapping("/insights")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<InsightDto>>> getInsights(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Actionable insights retrieved", managerDashboardCacheService.getInsights(emp)));
    }

    @Operation(summary = "Get Manager Quick Actions Panel Metadata")
    @GetMapping("/actions")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<QuickActionDto>>> getQuickActions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Quick actions retrieved", managerDashboardCacheService.getQuickActions(emp)));
    }

    @Operation(summary = "Get Manager Dashboard Notifications")
    @GetMapping("/notifications")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", managerDashboardCacheService.getNotifications(emp)));
    }

    @Operation(summary = "Get Team Schedule Snapshot")
    @GetMapping("/schedule")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<ScheduleSnapshotDto>> getScheduleSnapshot(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager or Admin access.", "AUTH_002"));
        }
        Employee emp = resolveEmployee(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Schedule snapshot retrieved", managerDashboardCacheService.getScheduleSnapshot(emp)));
    }
}
