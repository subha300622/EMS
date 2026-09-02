package com.example.ems.goal.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.goal.dto.CreateGoalRequest;
import com.example.ems.goal.dto.GoalResponse;
import com.example.ems.goal.dto.UpdateGoalRequest;
import com.example.ems.goal.service.GoalService;
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
@RequestMapping("/api/v1/goals")
@CrossOrigin("*")
@Tag(name = "Goal Management", description = "Core Domain APIs for Goal Creation, Lifecycle Commands, and Administration")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.example.ems.goal.service.GoalProgressService progressService;

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
        if (user == null)
            return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    @Operation(summary = "Create Goal", description = "Creates a new goal with tenant-isolated scope and optional approval workflow")
    @PostMapping

    public ResponseEntity<ApiResponse<Object>> createGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CreateGoalRequest request) {

        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        Long actorId = emp != null ? emp.getId() : (user != null ? user.getId() : 1L);
        String actorName = emp != null ? emp.getFirstName() + " " + emp.getLastName()
                : (user != null && user.getFullName() != null ? user.getFullName() : "System Admin");
        String actorRole = user != null && user.getRole() != null ? user.getRole().getName() : "ADMIN";

        GoalResponse response = goalService.createGoal(request, actorId, actorName, actorRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", response));
    }

    @Operation(summary = "Get Goal by ID", description = "Retrieves details of a specific goal")
    @GetMapping("/{goalId:\\d+}")

    public ResponseEntity<ApiResponse<Object>> getGoalById(@PathVariable("goalId") Long goalId) {
        GoalResponse response = goalService.getGoalById(goalId);
        return ResponseEntity.ok(ApiResponse.success("Goal retrieved successfully", response));
    }

    @Operation(summary = "List All Goals", description = "Retrieves paginated goals for the active tenant organization")
    @GetMapping

    public ResponseEntity<ApiResponse<Object>> getAllGoals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GoalResponse> goals = goalService.getAllGoals(pageable);
        return ResponseEntity.ok(ApiResponse.success("Goals retrieved successfully", goals));
    }

    @Operation(summary = "Update Goal Details", description = "Updates goal metadata without mutating state lifecycle status")
    @PutMapping("/{goalId:\\d+}")

    public ResponseEntity<ApiResponse<Object>> updateGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId,
            @RequestBody UpdateGoalRequest request) {

        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        Long actorId = emp != null ? emp.getId() : 1L;
        String actorName = emp != null ? emp.getFirstName() + " " + emp.getLastName()
                : (user != null && user.getFullName() != null ? user.getFullName() : "System Admin");
        String actorRole = user != null && user.getRole() != null ? user.getRole().getName() : "ADMIN";

        GoalResponse response = goalService.updateGoal(goalId, request, actorId, actorName, actorRole);
        return ResponseEntity.ok(ApiResponse.success("Goal updated successfully", response));
    }

    @Operation(summary = "Delete Goal", description = "Soft deletes a goal")
    @DeleteMapping("/{goalId:\\d+}")

    public ResponseEntity<ApiResponse<Object>> deleteGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId) {

        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        Long actorId = emp != null ? emp.getId() : 1L;
        String actorName = emp != null ? emp.getFirstName() + " " + emp.getLastName()
                : (user != null && user.getFullName() != null ? user.getFullName() : "System Admin");
        String actorRole = user != null && user.getRole() != null ? user.getRole().getName() : "ADMIN";

        goalService.deleteGoal(goalId, actorId, actorName, actorRole);
        return ResponseEntity.ok(ApiResponse.success("Goal deleted successfully", null));
    }

    // --- Command State Machine Endpoints ---

    @Operation(summary = "Activate Goal", description = "Transitions goal status to ACTIVE")
    @PostMapping("/{goalId:\\d+}/activate")

    public ResponseEntity<ApiResponse<Object>> activateGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId) {
        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        GoalResponse response = goalService.activateGoal(goalId, emp != null ? emp.getId() : 1L,
                emp != null ? emp.getFirstName() : "Admin", "USER");
        return ResponseEntity.ok(ApiResponse.success("Goal activated successfully", response));
    }

    @Operation(summary = "Hold Goal", description = "Puts active goal on hold")
    @PostMapping("/{goalId:\\d+}/hold")

    public ResponseEntity<ApiResponse<Object>> holdGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId) {
        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        GoalResponse response = goalService.holdGoal(goalId, emp != null ? emp.getId() : 1L,
                emp != null ? emp.getFirstName() : "Admin", "USER");
        return ResponseEntity.ok(ApiResponse.success("Goal put on hold", response));
    }

    @Operation(summary = "Resume Goal", description = "Resumes goal from ON_HOLD to ACTIVE")
    @PostMapping("/{goalId:\\d+}/resume")

    public ResponseEntity<ApiResponse<Object>> resumeGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId) {
        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        GoalResponse response = goalService.resumeGoal(goalId, emp != null ? emp.getId() : 1L,
                emp != null ? emp.getFirstName() : "Admin", "USER");
        return ResponseEntity.ok(ApiResponse.success("Goal resumed successfully", response));
    }

    @Operation(summary = "Complete Goal", description = "Marks goal as COMPLETED or triggers completion approval workflow")
    @PostMapping("/{goalId:\\d+}/complete")

    public ResponseEntity<ApiResponse<Object>> completeGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId) {
        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        GoalResponse response = goalService.completeGoal(goalId, emp != null ? emp.getId() : 1L,
                emp != null ? emp.getFirstName() : "Admin", "USER");
        return ResponseEntity.ok(ApiResponse.success("Goal completion processed successfully", response));
    }

    @Operation(summary = "Cancel Goal", description = "Cancels a goal")
    @PostMapping("/{goalId:\\d+}/cancel")

    public ResponseEntity<ApiResponse<Object>> cancelGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId) {
        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        GoalResponse response = goalService.cancelGoal(goalId, emp != null ? emp.getId() : 1L,
                emp != null ? emp.getFirstName() : "Admin", "USER");
        return ResponseEntity.ok(ApiResponse.success("Goal cancelled", response));
    }

    @Operation(summary = "Reopen Goal", description = "Reopens a completed or cancelled goal to ACTIVE status")
    @PostMapping("/{goalId:\\d+}/reopen")

    public ResponseEntity<ApiResponse<Object>> reopenGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("goalId") Long goalId) {
        User user = resolveUser(authHeader);
        Employee emp = resolveEmployee(user);
        GoalResponse response = goalService.reopenGoal(goalId, emp != null ? emp.getId() : 1L,
                emp != null ? emp.getFirstName() : "Admin", "USER");
        return ResponseEntity.ok(ApiResponse.success("Goal reopened successfully", response));
    }

    @Operation(summary = "Get Goal History", description = "Retrieves state change and progress history for a goal")
    @GetMapping("/{goalId:\\d+}/history")
    public ResponseEntity<ApiResponse<Object>> getGoalHistory(@PathVariable("goalId") Long goalId) {
        var history = progressService.getProgressHistory(goalId);
        return ResponseEntity.ok(ApiResponse.success("Goal history retrieved successfully", history));
    }
}
