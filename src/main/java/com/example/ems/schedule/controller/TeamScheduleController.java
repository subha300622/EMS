package com.example.ems.schedule.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.schedule.dto.AssignShiftRequest;
import com.example.ems.schedule.dto.TeamScheduleResponse;
import com.example.ems.schedule.service.TeamScheduleService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/team-schedule")
@CrossOrigin("*")
@Tag(name = "Team Scheduling Module")
public class TeamScheduleController {

    @Autowired
    private TeamScheduleService teamScheduleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

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

    private boolean checkReadPermission(User user) {
        if (user == null) return false;
        String email = user.getWorkEmail();
        return roleService.isSuperAdmin(email)
                || roleService.hasPermission(email, "employee.schedule.read")
                || roleService.hasPermission(email, "attendance.team.read")
                || roleService.hasRoleOrGreater(user, "MANAGER");
    }

    private boolean checkWritePermission(User user) {
        if (user == null) return false;
        String email = user.getWorkEmail();
        return roleService.isSuperAdmin(email)
                || roleService.hasPermission(email, "employee.schedule.write")
                || roleService.hasPermission(email, "attendance.team.manage")
                || roleService.hasRoleOrGreater(user, "MANAGER");
    }

    @Operation(summary = "Get Team Schedule Dashboard Grid & KPIs", description = "Retrieves coverage KPIs, paginated weekly grid, swap requests, and overtime logs.")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTeamSchedule(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long managerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkReadPermission(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires read permission", "AUTH_002"));
        }

        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

            TeamScheduleResponse data = teamScheduleService.getTeamSchedule(
                    start, end, departmentId, managerId, page, size, user);
            return ResponseEntity.ok(ApiResponse.success("Team schedule retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Paint / Assign Shift to Employee", description = "Creates or updates shift assignment. NONE deletes any existing shift.")
    @PostMapping("/shifts")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> assignShift(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AssignShiftRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkWritePermission(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires write permission", "AUTH_002"));
        }

        try {
            teamScheduleService.assignShift(request);
            return ResponseEntity.ok(ApiResponse.success("Shift assigned successfully", null));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Approve Shift Swap / Change Request", description = "Approves a pending change request and applies the new shift schedule.")
    @PostMapping("/swap-requests/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveSwap(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkWritePermission(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires write permission", "AUTH_002"));
        }

        try {
            teamScheduleService.approveSwap(id);
            return ResponseEntity.ok(ApiResponse.success("Swap request approved successfully", null));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    @Operation(summary = "Reject Shift Swap / Change Request", description = "Rejects a pending shift change request.")
    @PostMapping("/swap-requests/{id}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectSwap(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkWritePermission(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires write permission", "AUTH_002"));
        }

        try {
            teamScheduleService.rejectSwap(id);
            return ResponseEntity.ok(ApiResponse.success("Swap request rejected successfully", null));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }
}
