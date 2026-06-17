package com.example.ems.performance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.entity.Goal;
import com.example.ems.performance.repository.GoalRepository;
import com.example.ems.performance.service.GoalService;
import com.example.ems.security.service.JwtService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/goals")
@CrossOrigin("*")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

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

    private Employee resolveEmployee(User currentUser) {
        if (currentUser == null) return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    private boolean hasAccessToGoal(User currentUser, Goal goal, String selfPermission, String generalPermission) {
        if (roleService.isSuperAdmin(currentUser.getWorkEmail())) {
            return true;
        }
        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return false;
        }
        boolean isOwner = goal.getEmployee().getId().equals(employee.getId());
        boolean isManager = goal.getManager() != null && goal.getManager().getId().equals(employee.getId());

        if (isOwner) {
            return roleService.hasPermission(currentUser.getWorkEmail(), selfPermission)
                    || roleService.hasPermission(currentUser.getWorkEmail(), generalPermission);
        }
        if (isManager) {
            return roleService.hasPermission(currentUser.getWorkEmail(), generalPermission);
        }
        return roleService.hasPermission(currentUser.getWorkEmail(), generalPermission);
    }

    // 1. Create Goal
    @PostMapping
    public ResponseEntity<?> createGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CreateGoalRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Employee employee = resolveEmployee(currentUser);
        boolean isSelf = employee != null && request.getEmployeeId().equals(employee.getId());
        
        boolean allowed = roleService.isSuperAdmin(currentUser.getWorkEmail())
                || (isSelf && roleService.hasPermission(currentUser.getWorkEmail(), "goal.self.update"))
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.create");

        if (!allowed) {
            return forbiddenResponse("goal.create");
        }

        try {
            Map<String, Object> data = goalService.createGoal(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Goal created successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 2. Get My Goals
    @GetMapping("/my")
    public ResponseEntity<?> getMyGoals(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        boolean allowed = roleService.isSuperAdmin(currentUser.getWorkEmail())
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.self.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.read");

        if (!allowed) {
            return forbiddenResponse("goal.self.read");
        }

        try {
            Map<String, Object> data = goalService.getMyGoals(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Goals retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 3. Get Goal Details
    @GetMapping("/{goalId}")
    public ResponseEntity<?> getGoalDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        if (!hasAccessToGoal(currentUser, goal, "goal.self.read", "goal.read")) {
            return forbiddenResponse("goal.read");
        }

        try {
            Map<String, Object> data = goalService.getGoalDetails(goalId);
            return ResponseEntity.ok(ApiResponse.success("Goal details retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 4. Update Goal
    @PutMapping("/{goalId}")
    public ResponseEntity<?> updateGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId,
            @RequestBody UpdateGoalRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        if (!hasAccessToGoal(currentUser, goal, "goal.self.update", "goal.update")) {
            return forbiddenResponse("goal.update");
        }

        try {
            goalService.updateGoal(goalId, request);
            return ResponseEntity.ok(ApiResponse.success("Goal updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 5. Update Goal Progress
    @PatchMapping("/{goalId}/progress")
    public ResponseEntity<?> updateGoalProgress(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId,
            @Valid @RequestBody UpdateGoalProgressRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        if (!hasAccessToGoal(currentUser, goal, "goal.self.update", "goal.update")) {
            return forbiddenResponse("goal.update");
        }

        if (request.getProgress() == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("progress field is required", "VAL_001"));
        }

        try {
            Map<String, Object> data = goalService.updateGoalProgress(goalId, request, currentUser.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Progress updated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }

    }

    // 6. Submit Goal for Approval
    @PostMapping("/{goalId}/submit")
    public ResponseEntity<?> submitGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        if (!hasAccessToGoal(currentUser, goal, "goal.self.update", "goal.submit")) {
            return forbiddenResponse("goal.submit");
        }

        try {
            goalService.submitGoal(goalId);
            return ResponseEntity.ok(ApiResponse.success("Goal submitted for manager review", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 7. Approve Goal
    @PostMapping("/{goalId}/approve")
    public ResponseEntity<?> approveGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId,
            @RequestBody(required = false) GoalDecisionRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        boolean isManager = employee != null && goal.getManager() != null && goal.getManager().getId().equals(employee.getId());
        
        boolean allowed = roleService.isSuperAdmin(currentUser.getWorkEmail())
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.approve")
                || isManager;

        if (!allowed) {
            return forbiddenResponse("goal.approve");
        }

        try {
            goalService.approveGoal(goalId, request, employee);
            return ResponseEntity.ok(ApiResponse.success("Goal approved successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 8. Reject Goal
    @PostMapping("/{goalId}/reject")
    public ResponseEntity<?> rejectGoal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId,
            @RequestBody(required = false) GoalDecisionRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        boolean isManager = employee != null && goal.getManager() != null && goal.getManager().getId().equals(employee.getId());
        
        boolean allowed = roleService.isSuperAdmin(currentUser.getWorkEmail())
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.reject")
                || isManager;

        if (!allowed) {
            return forbiddenResponse("goal.reject");
        }

        try {
            goalService.rejectGoal(goalId, request, employee);
            return ResponseEntity.ok(ApiResponse.success("Goal rejected", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 9. Goal Comments
    @PostMapping("/{goalId}/comments")
    public ResponseEntity<?> addComment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId,
            @Valid @RequestBody GoalCommentRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        if (!hasAccessToGoal(currentUser, goal, "goal.self.update", "goal.update")) {
            return forbiddenResponse("goal.update");
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) return unauthorizedResponse();

        try {
            Map<String, Object> data = goalService.addComment(goalId, request, employee);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment added successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 10. Goal Progress History
    @GetMapping("/{goalId}/history")
    public ResponseEntity<?> getGoalHistory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long goalId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        Goal goal = goalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + goalId, "GOAL_001"));
        }

        if (!hasAccessToGoal(currentUser, goal, "goal.self.read", "goal.read")) {
            return forbiddenResponse("goal.read");
        }

        try {
            List<Map<String, Object>> data = goalService.getHistory(goalId);
            return ResponseEntity.ok(ApiResponse.success("Progress history retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 11. Goal Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        boolean allowed = roleService.isSuperAdmin(currentUser.getWorkEmail())
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.self.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.read");

        if (!allowed) {
            return forbiddenResponse("goal.self.read");
        }

        boolean hasAllAccess = roleService.isSuperAdmin(currentUser.getWorkEmail())
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.read");

        try {
            Map<String, Object> data = goalService.getDashboardData(currentUser.getWorkEmail(), hasAllAccess);
            return ResponseEntity.ok(ApiResponse.success("Goal dashboard data retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }

    // 12. Goal Analytics
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();

        boolean allowed = roleService.isSuperAdmin(currentUser.getWorkEmail())
                || roleService.hasPermission(currentUser.getWorkEmail(), "goal.analytics.read");

        if (!allowed) {
            return forbiddenResponse("goal.analytics.read");
        }

        try {
            Map<String, Object> data = goalService.getAnalytics();
            return ResponseEntity.ok(ApiResponse.success("Goal analytics data retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "GOAL_ERR"));
        }
    }
}
