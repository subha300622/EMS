package com.example.ems.employee.controller;

import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.teamleader.*;
import com.example.ems.employee.service.TeamLeaderDashboardService;
import com.example.ems.security.service.PermissionCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/team-leader", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Team Leader Dashboard", description = "Team Leader self-service dashboard, team roster, attendance, and leave recommendations")
public class TeamLeaderDashboardController {

    @Autowired
    private TeamLeaderDashboardService dashboardService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    private boolean isNotAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName());
    }

    // 1. Main Dashboard
    @Operation(summary = "Get Team Leader Dashboard", description = "Retrieves aggregated metrics, team roster, and pending leave reviews for team leader.")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required to access team leader dashboard.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(PermissionRegistry.TEAM_DASHBOARD_VIEW, PermissionRegistry.TEAM_DASHBOARD_READ);
        TeamLeaderDashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(response);
    }

    // 2. Team Members Roster
    @Operation(summary = "Get Team Members", description = "Retrieves direct reports belonging to the authenticated team leader's team.")
    @GetMapping("/team-members")
    public ResponseEntity<?> getTeamMembers() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.TEAM_DASHBOARD_VIEW,
                PermissionRegistry.TEAM_DASHBOARD_READ,
                PermissionRegistry.TEAM_MEMBER_READ
        );
        List<TeamMemberRosterDto> response = dashboardService.getTeamMembers();
        return ResponseEntity.ok(response);
    }

    // 3. Team Member Details (with IDOR check)
    @Operation(summary = "Get Team Member by ID", description = "Retrieves a single team member's details, verifying membership under the team leader.")
    @GetMapping("/team-members/{id}")
    public ResponseEntity<?> getTeamMemberById(@PathVariable("id") Long id) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.TEAM_DASHBOARD_VIEW,
                PermissionRegistry.TEAM_DASHBOARD_READ,
                PermissionRegistry.TEAM_MEMBER_READ
        );
        TeamMemberRosterDto response = dashboardService.getTeamMemberById(id);
        return ResponseEntity.ok(response);
    }

    // 4. Leave Review Recommendation
    @Operation(summary = "Recommend or Not Recommend Leave", description = "Team leader submits a recommendation on a team member's pending leave request.")
    @PostMapping("/leave-reviews/{leaveId}/recommend")
    public ResponseEntity<?> recommendLeave(@PathVariable("leaveId") Long leaveId,
                                            @RequestBody @Valid TeamLeaveRecommendationRequest request) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.TEAM_DASHBOARD_VIEW,
                PermissionRegistry.TEAM_LEAVE_RECOMMEND,
                PermissionRegistry.LEAVE_TEAM_APPROVE
        );
        dashboardService.recommendLeave(leaveId, request);
        return ResponseEntity.ok().build();
    }

    // 5. Today's Attendance
    @Operation(summary = "Get Team Attendance Today", description = "Aggregates today's attendance status breakdown for all team members.")
    @GetMapping("/attendance/today")
    public ResponseEntity<?> getAttendanceToday() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.TEAM_DASHBOARD_VIEW,
                PermissionRegistry.TEAM_DASHBOARD_READ,
                "attendance.team.read"
        );
        TeamLeaderAttendanceTodayResponse response = dashboardService.getAttendanceToday();
        return ResponseEntity.ok(response);
    }

    // 6. Performance Summary
    @Operation(summary = "Get Team Performance", description = "Aggregates performance ratings and sprint delivery index for the team.")
    @GetMapping("/performance")
    public ResponseEntity<?> getPerformance() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.TEAM_DASHBOARD_VIEW,
                PermissionRegistry.TEAM_DASHBOARD_READ,
                "performance.read"
        );
        TeamLeaderPerformanceResponse response = dashboardService.getPerformance();
        return ResponseEntity.ok(response);
    }
}
