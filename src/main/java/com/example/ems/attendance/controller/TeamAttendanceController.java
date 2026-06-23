package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.TeamMemberAttendanceDto;
import com.example.ems.attendance.dto.TeamSummaryDto;
import com.example.ems.attendance.dto.TeamTrendDto;
import com.example.ems.attendance.service.TeamAttendanceService;
import com.example.ems.attendance.service.TeamResolutionService;
import com.example.ems.attendance.service.TeamAttendanceCalendarService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Team Attendance Analytics")
public class TeamAttendanceController {

    @Autowired
    private TeamResolutionService teamResolutionService;

    @Autowired
    private TeamAttendanceService teamAttendanceService;

    @Autowired
    private TeamAttendanceCalendarService teamAttendanceCalendarService;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    private Employee resolveEmployee(User currentUser) {
        if (currentUser == null) return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    private boolean isAdminOrHr(User user) {
        if (user == null) return false;
        String email = user.getWorkEmail();
        return roleService.hasPermission(email, "attendance.read")
                || roleService.hasPermission(email, "attendance.manage")
                || roleService.hasRoleOrGreater(user, "HR");
    }

    private ResponseEntity<?> checkAccessAndResolveEmployees(
            User currentUser, String scope, Long employeeId, Long managerId, Long departmentId, AtomicReference<List<Employee>> resolvedContainer) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee currentEmployee = resolveEmployee(currentUser);
        boolean isAdmin = isAdminOrHr(currentUser);

        // Scope parameter verification
        if ("employee".equalsIgnoreCase(scope) && employeeId == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Employee ID is required for employee scope", "VAL_001"));
        }
        if ("manager".equalsIgnoreCase(scope) && managerId == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Manager ID is required for manager scope", "VAL_001"));
        }
        if ("department".equalsIgnoreCase(scope) && departmentId == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Department ID is required for department scope", "VAL_001"));
        }

        // Strict role validation checks
        if (!isAdmin) {
            if ("global".equalsIgnoreCase(scope)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: Requires Admin/HR permissions for global scope.", "AUTH_002"));
            }
            if ("department".equalsIgnoreCase(scope)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: Requires Admin/HR permissions for department scope.", "AUTH_002"));
            }
            if ("employee".equalsIgnoreCase(scope)) {
                if (currentEmployee == null || !currentEmployee.getId().equals(employeeId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.error("Access Denied: You can only query your own employee attendance.", "AUTH_002"));
                }
            }
            if ("manager".equalsIgnoreCase(scope)) {
                if (currentEmployee == null || !currentEmployee.getId().equals(managerId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.error("Access Denied: You can only query your own team's attendance.", "AUTH_002"));
                }
            }
        }

        try {
            resolvedContainer.set(teamResolutionService.resolveEmployees(scope, employeeId, managerId, departmentId, currentUser));
            return null;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Team Attendance list", description = "Retrieves team member details and their sparse logs for a range or specific date.")
    @GetMapping("/team-attendance")
    public ResponseEntity<?> getTeamAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "scope", defaultValue = "global") String scope,
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestParam(name = "managerId", required = false) Long managerId,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "date", required = false) String date) {

        User currentUser = resolveUser(authHeader);
        AtomicReference<List<Employee>> resolvedContainer = new AtomicReference<>();
        ResponseEntity<?> accessError = checkAccessAndResolveEmployees(
                currentUser, scope, employeeId, managerId, departmentId, resolvedContainer);
        if (accessError != null) return accessError;

        LocalDate startDate = date != null ? LocalDate.parse(date) : (from != null ? LocalDate.parse(from) : LocalDate.now());
        LocalDate endDate = date != null ? LocalDate.parse(date) : (to != null ? LocalDate.parse(to) : LocalDate.now());

        if (ChronoUnit.DAYS.between(startDate, endDate) > 31) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Date range cannot exceed 31 days", "VAL_002"));
        }

        List<TeamMemberAttendanceDto> data = teamAttendanceService.getTeamAttendance(resolvedContainer.get(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Team attendance retrieved successfully", data));
    }

    @Operation(summary = "Get Daily Team Attendance Summary", description = "Dashboard snapshot stats counts (present, absent, late, onLeave).")
    @GetMapping("/team-attendance/summary")
    public ResponseEntity<?> getTeamAttendanceSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "scope", defaultValue = "global") String scope,
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestParam(name = "managerId", required = false) Long managerId,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "date", required = false) String date) {

        User currentUser = resolveUser(authHeader);
        AtomicReference<List<Employee>> resolvedContainer = new AtomicReference<>();
        ResponseEntity<?> accessError = checkAccessAndResolveEmployees(
                currentUser, scope, employeeId, managerId, departmentId, resolvedContainer);
        if (accessError != null) return accessError;

        LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        TeamSummaryDto summary = teamAttendanceService.getTeamAttendanceSummary(resolvedContainer.get(), queryDate);
        return ResponseEntity.ok(ApiResponse.success("Team attendance summary retrieved", summary));
    }

    @Operation(summary = "Get Team Attendance Trend Analytics", description = "Date-by-date series trend aggregated on database side.")
    @GetMapping("/team-attendance/trend")
    public ResponseEntity<?> getTeamAttendanceTrend(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "scope", defaultValue = "global") String scope,
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestParam(name = "managerId", required = false) Long managerId,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to) {

        User currentUser = resolveUser(authHeader);
        AtomicReference<List<Employee>> resolvedContainer = new AtomicReference<>();
        ResponseEntity<?> accessError = checkAccessAndResolveEmployees(
                currentUser, scope, employeeId, managerId, departmentId, resolvedContainer);
        if (accessError != null) return accessError;

        LocalDate startDate = from != null ? LocalDate.parse(from) : LocalDate.now().minusDays(7);
        LocalDate endDate = to != null ? LocalDate.parse(to) : LocalDate.now();

        if (ChronoUnit.DAYS.between(startDate, endDate) > 31) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Date range cannot exceed 31 days", "VAL_002"));
        }

        TeamTrendDto trend = teamAttendanceService.getTeamAttendanceTrend(resolvedContainer.get(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Team attendance trend retrieved", trend));
    }

    @Operation(summary = "Get Monthly Team Attendance Calendar Grid", description = "Retrieves calendar grid with daily aggregates and member logs.")
    @GetMapping("/team-attendance/calendar/monthly")
    public ResponseEntity<?> getTeamMonthlyCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "managerId", required = false) Long managerId,
            @RequestParam(name = "month", required = false) String month,
            @RequestParam(name = "view", defaultValue = "full") String view) {

        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Month parameter in YYYY-MM format is required", "VAL_001"));
        }

        User currentUser = resolveUser(authHeader);
        String scope = "global";
        if (departmentId != null) {
            scope = "department";
        } else if (managerId != null) {
            scope = "manager";
        }

        AtomicReference<List<Employee>> resolvedContainer = new AtomicReference<>();
        ResponseEntity<?> accessError = checkAccessAndResolveEmployees(
                currentUser, scope, null, managerId, departmentId, resolvedContainer);
        if (accessError != null) return accessError;

        LocalDate startOfMonth = LocalDate.parse(month + "-01");
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Object data = teamAttendanceCalendarService.getTeamMonthlyCalendar(
                resolvedContainer.get(), startOfMonth, endOfMonth, departmentId, managerId, view);
        return ResponseEntity.ok(ApiResponse.success("Monthly team calendar retrieved successfully", data));
    }

    @Operation(summary = "Get Monthly Team Heatmap", description = "Retrieves lightweight monthly heatmap showing daily presence percentages and colors.")
    @GetMapping("/team-attendance/calendar/heatmap")
    public ResponseEntity<?> getTeamHeatmap(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "managerId", required = false) Long managerId,
            @RequestParam(name = "month", required = false) String month) {

        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Month parameter in YYYY-MM format is required", "VAL_001"));
        }

        User currentUser = resolveUser(authHeader);
        String scope = "global";
        if (departmentId != null) {
            scope = "department";
        } else if (managerId != null) {
            scope = "manager";
        }

        AtomicReference<List<Employee>> resolvedContainer = new AtomicReference<>();
        ResponseEntity<?> accessError = checkAccessAndResolveEmployees(
                currentUser, scope, null, managerId, departmentId, resolvedContainer);
        if (accessError != null) return accessError;

        LocalDate startOfMonth = LocalDate.parse(month + "-01");
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Object data = teamAttendanceCalendarService.getTeamHeatmap(resolvedContainer.get(), startOfMonth, endOfMonth);
        return ResponseEntity.ok(ApiResponse.success("Monthly team heatmap retrieved successfully", data));
    }

    @Operation(summary = "Get Employee Monthly Calendar View", description = "Retrieves drilldown monthly calendar for a specific employee.")
    @GetMapping("/team-attendance/calendar/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeMonthlyCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(name = "employeeId") Long employeeId,
            @RequestParam(name = "month", required = false) String month) {

        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Month parameter in YYYY-MM format is required", "VAL_001"));
        }

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee currentEmployee = resolveEmployee(currentUser);
        boolean isAdmin = isAdminOrHr(currentUser);

        if (!isAdmin) {
            if (currentEmployee == null || !currentEmployee.getId().equals(employeeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: You can only query your own employee attendance.", "AUTH_002"));
            }
        }

        Employee targetEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (targetEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + employeeId, "VAL_001"));
        }

        LocalDate startOfMonth = LocalDate.parse(month + "-01");
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Object data = teamAttendanceCalendarService.getEmployeeMonthlyCalendar(targetEmployee, startOfMonth, endOfMonth);
        return ResponseEntity.ok(ApiResponse.success("Employee monthly calendar retrieved successfully", data));
    }

    @Operation(summary = "Get Department Calendar Summary", description = "Retrieves dashboard calendar KPIs for a month.")
    @GetMapping("/team-attendance/calendar/summary")
    public ResponseEntity<?> getDepartmentCalendarSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "managerId", required = false) Long managerId,
            @RequestParam(name = "month", required = false) String month) {

        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Month parameter in YYYY-MM format is required", "VAL_001"));
        }

        User currentUser = resolveUser(authHeader);
        String scope = "global";
        if (departmentId != null) {
            scope = "department";
        } else if (managerId != null) {
            scope = "manager";
        }

        AtomicReference<List<Employee>> resolvedContainer = new AtomicReference<>();
        ResponseEntity<?> accessError = checkAccessAndResolveEmployees(
                currentUser, scope, null, managerId, departmentId, resolvedContainer);
        if (accessError != null) return accessError;

        LocalDate startOfMonth = LocalDate.parse(month + "-01");
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Object data = teamAttendanceCalendarService.getDepartmentCalendarSummary(
                resolvedContainer.get(), startOfMonth, endOfMonth, departmentId, managerId);
        return ResponseEntity.ok(ApiResponse.success("Department calendar summary retrieved successfully", data));
    }
}
