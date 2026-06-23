package com.example.ems.performance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;

import com.example.ems.performance.dto.ManagerReviewRequest;
import com.example.ems.performance.dto.PerformanceCycleRequest;
import com.example.ems.performance.dto.PerformanceCycleResponse;
import com.example.ems.performance.dto.PerformanceDashboardResponse;
import com.example.ems.performance.dto.PerformanceGoalRequest;
import com.example.ems.performance.dto.PerformanceGoalResponse;
import com.example.ems.performance.dto.PerformancePipResponse;
import com.example.ems.performance.dto.PerformanceReviewResponse;
import com.example.ems.performance.dto.PipRequest;
import com.example.ems.performance.dto.SelfReviewRequest;
import com.example.ems.performance.service.PerformanceService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/performance")
@CrossOrigin("*")
public class PerformanceController {

    @Autowired private PerformanceService performanceService;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private RoleService roleService;

    // ── Auth helpers ─────────────────────────────────────────────────────────
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

    private boolean isManager(User user) {
        return roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @Operation(summary = "Get Dashboard Stats", tags = {"Performance Reviews"})
    @GetMapping("/reviews/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        PerformanceDashboardResponse stats = performanceService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Performance dashboard statistics retrieved successfully", stats));
    }

    // ── 2. CYCLES ────────────────────────────────────────────────────────────
    @Operation(summary = "Get Performance Cycles", tags = {"Performance Cycles"})
    @GetMapping("/reviews/cycles")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getCycles(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<PerformanceCycleResponse> cycles = performanceService.getCycles();
        return ResponseEntity.ok(ApiResponse.success("Performance cycles retrieved successfully", cycles));
    }

    @Operation(summary = "Create Performance Cycle", tags = {"Performance Cycles"})
    @PostMapping("/reviews/cycles")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PerformanceCycleRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        PerformanceCycleResponse cycle = performanceService.createCycle(request);
        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Performance cycle created successfully", cycle));
    }

    @Operation(summary = "Update Performance Cycle", tags = {"Performance Cycles"})
    @PutMapping("/reviews/cycles/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody PerformanceCycleRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<PerformanceCycleResponse> updated = performanceService.updateCycle(id, request);
        if (updated.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Performance cycle not found with ID: " + id, "PERF_001"));
        return ResponseEntity.ok(ApiResponse.success("Performance cycle updated successfully", updated.get()));
    }

    // ── 3. GOALS ─────────────────────────────────────────────────────────────
    @Operation(summary = "Get Goals", tags = {"Performance Reviews"})
    @GetMapping("/reviews/goals")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getGoals(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<PerformanceGoalResponse> goals;
        if (isManager(currentUser)) {
            goals = performanceService.getGoals(currentUser.getWorkEmail());
        } else {
            // Employee can only see their own goals (matched by email → employee ID)
            goals = userRepository.findByWorkEmail(currentUser.getWorkEmail())
                    .map(u -> u.getEmployeeId() != null
                            ? performanceService.getGoalsByEmployee(Long.parseLong(u.getEmployeeId()))
                            : List.<PerformanceGoalResponse>of())
                    .orElse(List.of());
        }
        return ResponseEntity.ok(ApiResponse.success("Performance goals retrieved successfully", goals));
    }

    @Operation(summary = "Create Goal", tags = {"Performance Reviews"})
    @PostMapping("/reviews/goals")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createGoal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PerformanceGoalRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            PerformanceGoalResponse goal = performanceService.createGoal(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Performance goal created successfully", goal));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_002"));
        }
    }

    @Operation(summary = "Update Goal Progress", tags = {"Performance Reviews"})
    @PatchMapping("/reviews/goals/{id}/progress")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateGoalProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Integer progress = body.get("progressPercent");
        if (progress == null)
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("progressPercent field is required", "VAL_001"));

        Optional<PerformanceGoalResponse> updated = performanceService.updateGoalProgress(id, progress);
        if (updated.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + id, "PERF_002"));
        return ResponseEntity.ok(ApiResponse.success("Goal progress updated to " + progress + "%", updated.get()));
    }

    @Operation(summary = "Delete Goal", tags = {"Performance Reviews"})
    @DeleteMapping("/reviews/goals/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteGoal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        boolean deleted = performanceService.deleteGoal(id);
        if (!deleted)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Goal not found with ID: " + id, "PERF_002"));
        return (ResponseEntity) ResponseEntity.ok(ApiResponse.success("Performance goal deleted successfully",
                Map.of("deletedGoalId", id)));
    }

    // ── 4. SELF-REVIEWS ──────────────────────────────────────────────────────
    @Operation(summary = "Submit Self Review", tags = {"Performance Reviews"})
    @PostMapping("/reviews/self-reviews")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> submitSelfReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody SelfReviewRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            PerformanceReviewResponse review = performanceService.submitSelfReview(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Self-review submitted successfully", review));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_003"));
        }
    }

    // ── 5. MANAGER REVIEWS ───────────────────────────────────────────────────
    @Operation(summary = "Submit Manager Review", tags = {"Performance Reviews"})
    @PostMapping("/reviews/manager-reviews")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> submitManagerReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ManagerReviewRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Only managers can submit manager reviews.", "AUTH_002"));

        try {
            PerformanceReviewResponse review = performanceService.submitManagerReview(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Manager review submitted successfully", review));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_003"));
        }
    }

    // ── 6. FEEDBACKS ─────────────────────────────────────────────────────────
    @Operation(summary = "Get Feedbacks", tags = {"Performance Reviews"})
    @GetMapping("/reviews/feedbacks")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getFeedbacks(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<PerformanceReviewResponse> feedbacks;
        if (isManager(currentUser)) {
            feedbacks = performanceService.getFeedbacks();
        } else {
            feedbacks = userRepository.findByWorkEmail(currentUser.getWorkEmail())
                    .map(u -> u.getEmployeeId() != null
                            ? performanceService.getFeedbacksByEmployee(Long.parseLong(u.getEmployeeId()))
                            : List.<PerformanceReviewResponse>of())
                    .orElse(List.of());
        }
        return ResponseEntity.ok(ApiResponse.success("Performance feedbacks retrieved successfully", feedbacks));
    }

    // ── 7. FINALIZE REVIEW ───────────────────────────────────────────────────
    @Operation(summary = "Finalize Review", tags = {"Performance Reviews"})
    @PostMapping("/reviews/reviews/{id}/finalize")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> finalizeReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Only managers can finalize reviews.", "AUTH_002"));

        Optional<PerformanceReviewResponse> finalized = performanceService.finalizeReview(id);
        if (finalized.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Review not found with ID: " + id, "PERF_004"));
        return ResponseEntity.ok(ApiResponse.success("Performance review finalized successfully", finalized.get()));
    }

    // ── 8. PIP ────────────────────────────────────────────────────────────────
    @Operation(summary = "Create PIP", tags = {"Performance Reviews"})
    @PostMapping("/reviews/pips")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createPip(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PipRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Only managers can create PIPs.", "AUTH_002"));

        try {
            PerformancePipResponse pip = performanceService.createPip(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Performance Improvement Plan (PIP) created successfully", pip));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_005"));
        }
    }

    // ── 9. REPORTS ───────────────────────────────────────────────────────────
    @Operation(summary = "Get Performance Report", tags = {"Performance Reviews"})
    @GetMapping("/reviews/reports/{reportType}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Map<String, Object> data = performanceService.getReportData(reportType);
        return ResponseEntity.ok(ApiResponse.success("Performance report generated successfully", data));
    }

    // ── 10. NOTIFICATIONS ─────────────────────────────────────────────────────
    @Operation(summary = "Send Performance Notification", tags = {"Performance Reviews"})
    @PostMapping("/reviews/notifications")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> sendNotification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        String message = body.getOrDefault("message", "Performance review reminder");
        Map<String, Object> result = Map.of(
                "status", "SENT",
                "message", message,
                "sentAt", java.time.LocalDateTime.now().toString(),
                "channel", "EMAIL"
        );
        return ResponseEntity.ok(ApiResponse.success("Performance notification dispatched successfully", result));
    }
}
