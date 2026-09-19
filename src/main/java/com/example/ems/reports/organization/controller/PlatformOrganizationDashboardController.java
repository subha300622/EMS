package com.example.ems.reports.organization.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.reports.organization.DashboardFacade;
import com.example.ems.reports.organization.dto.ChartResponse;
import com.example.ems.reports.organization.dto.DashboardSummaryResponse;
import com.example.ems.reports.organization.dto.DistributionResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.ems.reports.organization.dto.UserActivityReportResponse;

@RestController
@RequestMapping("/api/v1/platform/dashboard/organizations")
@CrossOrigin("*")
@Tag(name = "Platform Organization Dashboard", description = "Dashboard analytics for platform admins")
public class PlatformOrganizationDashboardController {

    @Autowired
    private DashboardFacade dashboardFacade;

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

    private ResponseEntity<?> validateAccess(String authHeader, String requiredPermission) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(user.getWorkEmail(), requiredPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires platform reports view permission.", "AUTH_002"));
        }
        return null;
    }

    @Operation(summary = "Get organization dashboard summary")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard metrics loaded successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DashboardSummaryResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        DashboardSummaryResponse data = dashboardFacade.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics loaded successfully", data));
    }

    @Operation(summary = "Get organization growth trend chart data")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Growth trend loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/growth")
    public ResponseEntity<?> getGrowth(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        Map<String, ChartResponse> data = dashboardFacade.getGrowth();
        return ResponseEntity.ok(ApiResponse.success("Growth trend loaded successfully", data));
    }

    @Operation(summary = "Get organization status distribution for charts")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status distribution loaded successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DistributionResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/status-distribution")
    public ResponseEntity<?> getStatusDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<DistributionResponse> data = dashboardFacade.getStatusDistribution();
        return ResponseEntity.ok(ApiResponse.success("Status distribution loaded successfully", data));
    }

    @Operation(summary = "Get subscription plan distribution for charts")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription distribution loaded successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DistributionResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/subscription-distribution")
    public ResponseEntity<?> getSubscriptionDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<DistributionResponse> data = dashboardFacade.getSubscriptionDistribution();
        return ResponseEntity.ok(ApiResponse.success("Subscription distribution loaded successfully", data));
    }

    @Operation(summary = "Get employee range distribution for charts")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee distribution loaded successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DistributionResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/employee-distribution")
    public ResponseEntity<?> getEmployeeDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<DistributionResponse> data = dashboardFacade.getEmployeeDistribution();
        return ResponseEntity.ok(ApiResponse.success("Employee distribution loaded successfully", data));
    }

    @Operation(summary = "Get user activity report metrics")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Activity metrics loaded successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserActivityReportResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/activity")
    public ResponseEntity<?> getActivityReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        UserActivityReportResponse data = dashboardFacade.getActivityReport();
        return ResponseEntity.ok(ApiResponse.success("Activity metrics loaded successfully", data));
    }
}
