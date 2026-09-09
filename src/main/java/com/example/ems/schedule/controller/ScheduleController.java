package com.example.ems.schedule.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.entity.ScheduleStatus;
import com.example.ems.schedule.service.ScheduleManagementService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1/schedules")
@CrossOrigin("*")
@Tag(name = "Schedules Management Module", description = "REST APIs for Managing Employee Work Schedules")
public class ScheduleController {

    @Autowired
    private ScheduleManagementService scheduleManagementService;

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
                || roleService.hasPermission(email, "SCHEDULE_VIEW")
                || roleService.hasRoleOrGreater(user, "MANAGER");
    }

    private boolean checkWritePermission(User user, String permission) {
        if (user == null) return false;
        String email = user.getWorkEmail();
        return roleService.isSuperAdmin(email)
                || roleService.hasPermission(email, "employee.schedule.write")
                || roleService.hasPermission(email, permission)
                || roleService.hasRoleOrGreater(user, "MANAGER");
    }

    @Operation(summary = "Get Schedules List", description = "Retrieves paginated schedules with filters for date range, employee, team, department, and status.")
    @GetMapping
    public ResponseEntity<ApiResponse<ScheduleListResponse>> getSchedules(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkReadPermission(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule read permission", "AUTH_002"));
        }

        try {
            ScheduleListResponse response = scheduleManagementService.getSchedules(
                    user, fromDate, toDate, employeeId, teamId, departmentId, status, page, size);
            return ResponseEntity.ok(ApiResponse.success("Schedules retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Single Schedule", description = "Retrieves a specific schedule by public scheduleId.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleDto>> getScheduleById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String scheduleId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkReadPermission(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule read permission", "AUTH_002"));
        }

        try {
            ScheduleDto dto = scheduleManagementService.getScheduleById(user, scheduleId);
            return ResponseEntity.ok(ApiResponse.success("Schedule retrieved successfully", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Create Schedule", description = "Creates a new employee schedule.")
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleDto>> createSchedule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ScheduleCreateRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkWritePermission(user, "SCHEDULE_CREATE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule create permission", "AUTH_002"));
        }

        try {
            ScheduleDto created = scheduleManagementService.createSchedule(user, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Schedule created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    @Operation(summary = "Update Schedule", description = "Updates an existing schedule by public scheduleId.")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleDto>> updateSchedule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String scheduleId,
            @RequestBody ScheduleUpdateRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkWritePermission(user, "SCHEDULE_UPDATE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule update permission", "AUTH_002"));
        }

        try {
            ScheduleDto updated = scheduleManagementService.updateSchedule(user, scheduleId, request);
            return ResponseEntity.ok(ApiResponse.success("Schedule updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    @Operation(summary = "Delete Schedule", description = "Deletes a schedule by public scheduleId.")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String scheduleId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkWritePermission(user, "SCHEDULE_DELETE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule delete permission", "AUTH_002"));
        }

        try {
            scheduleManagementService.deleteSchedule(user, scheduleId);
            return ResponseEntity.ok(ApiResponse.success("Schedule deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Employee Schedules", description = "Retrieves all schedules assigned to a specific employee.")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getSchedulesByEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkReadPermission(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule read permission", "AUTH_002"));
        }

        try {
            List<ScheduleDto> list = scheduleManagementService.getSchedulesByEmployee(user, employeeId);
            return ResponseEntity.ok(ApiResponse.success("Employee schedules retrieved successfully", list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Employee Availability", description = "Checks if an employee is available or on approved leave on a given date.")
    @GetMapping("/employee/{employeeId}/availability")
    public ResponseEntity<ApiResponse<EmployeeAvailabilityDto>> getEmployeeAvailability(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkReadPermission(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule read permission", "AUTH_002"));
        }

        LocalDate targetDate = date != null ? date : LocalDate.now();
        try {
            EmployeeAvailabilityDto dto = scheduleManagementService.getEmployeeAvailability(user, employeeId, targetDate);
            return ResponseEntity.ok(ApiResponse.success("Employee availability retrieved successfully", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Team Schedules", description = "Retrieves all schedules assigned to members of a team.")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getSchedulesByTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkReadPermission(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule read permission", "AUTH_002"));
        }

        try {
            List<ScheduleDto> list = scheduleManagementService.getSchedulesByTeam(user, teamId);
            return ResponseEntity.ok(ApiResponse.success("Team schedules retrieved successfully", list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Department Schedules", description = "Retrieves all schedules assigned to members of a department.")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getSchedulesByDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkReadPermission(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires schedule read permission", "AUTH_002"));
        }

        try {
            List<ScheduleDto> list = scheduleManagementService.getSchedulesByDepartment(user, departmentId);
            return ResponseEntity.ok(ApiResponse.success("Department schedules retrieved successfully", list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VAL_001"));
        }
    }
}
