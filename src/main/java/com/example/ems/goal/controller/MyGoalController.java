package com.example.ems.goal.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.goal.dto.*;
import com.example.ems.goal.service.*;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my/goals")
@CrossOrigin("*")
@Tag(name = "Employee Goal Self-Service", description = "Dedicated Employee Role APIs for Goal Self-Service Actions")
public class MyGoalController {

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalProgressService progressService;

    @Autowired
    private GoalEffortService effortService;

    @Autowired
    private GoalCommentService commentService;

    @Autowired
    private GoalDashboardService dashboardService;

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

    private Employee resolveEmployee(User user) {
        if (user == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    @Operation(summary = "Get My Assigned Goals", description = "Retrieves goals assigned to the authenticated employee")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyGoals(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        Long empId = currentEmp != null ? currentEmp.getId() : currentUser.getId();

        Pageable pageable = PageRequest.of(page, size);
        Page<GoalResponse> goals = goalService.getMyGoals(empId, pageable);

        return ResponseEntity.ok(ApiResponse.success("My goals retrieved successfully", goals));
    }

    @Operation(summary = "Get My Goal Details", description = "Retrieves details of a specific assigned goal")
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Object>> getMyGoalById(@PathVariable("goalId") Long goalId) {
        GoalResponse goal = goalService.getGoalById(goalId);
        return ResponseEntity.ok(ApiResponse.success("Goal details retrieved successfully", goal));
    }

    @Operation(summary = "Submit My Goal Progress", description = "Submits progress entry for assigned goal")
    @PostMapping("/{goalId}/progress")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> submitMyGoalProgress(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalProgressRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        Long empId = currentEmp != null ? currentEmp.getId() : currentUser.getId();
        String empName = currentEmp != null ? currentEmp.getFirstName() + " " + currentEmp.getLastName() : (currentUser.getFullName() != null ? currentUser.getFullName() : "Employee");

        var entry = progressService.addProgressUpdate(goalId, request, empId, empName, "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Progress update submitted successfully", entry));
    }

    @Operation(summary = "Log My Goal Effort", description = "Logs actual effort hours spent on assigned goal")
    @PostMapping("/{goalId}/efforts")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> logMyGoalEffort(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalEffortRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        Long empId = currentEmp != null ? currentEmp.getId() : currentUser.getId();
        String empName = currentEmp != null ? currentEmp.getFirstName() + " " + currentEmp.getLastName() : (currentUser.getFullName() != null ? currentUser.getFullName() : "Employee");

        var entry = effortService.logEffort(goalId, request, empId, empName, "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Effort logged successfully", entry));
    }

    @Operation(summary = "Add My Goal Comment", description = "Adds a discussion comment on assigned goal")
    @PostMapping("/{goalId}/comments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> addMyGoalComment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalCommentRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        Long empId = currentEmp != null ? currentEmp.getId() : currentUser.getId();
        String empName = currentEmp != null ? currentEmp.getFirstName() + " " + currentEmp.getLastName() : (currentUser.getFullName() != null ? currentUser.getFullName() : "Employee");

        var comment = commentService.addComment(goalId, request, empId, empName, "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Comment posted successfully", comment));
    }

    @Operation(summary = "Get My Goal Dashboard", description = "Calculates personal goal metrics for authenticated employee")
    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyGoalDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        Long empId = currentEmp != null ? currentEmp.getId() : currentUser.getId();

        GoalDashboardResponse response = dashboardService.getEmployeeDashboard(empId);
        return ResponseEntity.ok(ApiResponse.success("My goal dashboard retrieved successfully", response));
    }
}
