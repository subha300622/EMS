package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.service.HrDashboardService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/hr/dashboard")
@CrossOrigin("*")
@Tag(name = "HR Dashboard")
public class HrDashboardController {

    @Autowired
    private HrDashboardService hrDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

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
        if (user == null)
            return false;
        String email = user.getWorkEmail();
        return roleService.hasPermission(email, "recruitment.manage")
                || roleService.hasPermission(email, "employee.update")
                || roleService.hasPermission(email, "employee.delete")
                || (user.getRole() != null && ("HR".equalsIgnoreCase(user.getRole().getName())
                        || "ADMIN".equalsIgnoreCase(user.getRole().getName())
                        || "SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName())
                        || "MANAGER".equalsIgnoreCase(user.getRole().getName())));
    }

    @Operation(summary = "Get Main HR Dashboard Summary")
    @GetMapping
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("HR Dashboard summary retrieved", hrDashboardService.getDashboardSummary()));
    }

    @Operation(summary = "Get Headcount KPI Card Data")
    @GetMapping("/headcount")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHeadcount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Headcount stats retrieved", hrDashboardService.getHeadcountStats()));
    }

    @Operation(summary = "Get New Hires KPI Card Data")
    @GetMapping("/new-hires")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNewHires(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("New hires stats retrieved", hrDashboardService.getNewHiresStats()));
    }

    @Operation(summary = "Get Attrition KPI Card Data")
    @GetMapping("/attrition")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttrition(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Attrition stats retrieved", hrDashboardService.getAttritionStats()));
    }

    @Operation(summary = "Get Open Positions KPI Card Data")
    @GetMapping("/open-positions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOpenPositions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Open positions stats retrieved", hrDashboardService.getOpenPositionsStats()));
    }

    @Operation(summary = "Get Headcount Trend Chart")
    @GetMapping("/charts/headcount-trend")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHeadcountTrend(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "period", defaultValue = "6months") String period) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Headcount trend chart retrieved", hrDashboardService.getHeadcountTrend(period)));
    }

    @Operation(summary = "Get Employee Department Breakdown Pie Chart")
    @GetMapping("/charts/employee-breakdown")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeBreakdown(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Employee breakdown chart retrieved", hrDashboardService.getEmployeeBreakdown()));
    }

    @Operation(summary = "Get Leave Approvals Widget Data")
    @GetMapping("/pending-leaves")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Pending leaves retrieved", hrDashboardService.getPendingLeaves()));
    }

    @Operation(summary = "Get Recent Hires Widget Data")
    @GetMapping("/recent-hires")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentHires(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Recent hires retrieved", hrDashboardService.getRecentHires()));
    }

    @Operation(summary = "Get Attendance by Department Widget Data")
    @GetMapping("/attendance-by-department")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAttendanceByDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Attendance by department retrieved",
                hrDashboardService.getAttendanceByDepartment()));
    }

    @Operation(summary = "Get Retention Alerts Widget Data")
    @GetMapping("/retention-alerts")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRetentionAlerts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Retention alerts retrieved", hrDashboardService.getRetentionAlerts()));
    }

    @Operation(summary = "Global HR Search")
    @GetMapping("/search")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Search results retrieved", hrDashboardService.globalSearch(keyword)));
    }

    @Operation(summary = "Aggregated HR Dashboard Summary (Recommended)")
    @GetMapping("/summary")
    @Deprecated
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasAccess(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Admin access.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("HR Dashboard aggregated summary retrieved",
                hrDashboardService.getDashboardSummaryAggregation()));
    }
}
