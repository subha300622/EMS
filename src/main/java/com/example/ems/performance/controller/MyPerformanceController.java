package com.example.ems.performance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.service.MyPerformanceService;
import com.example.ems.security.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my-performance")
@CrossOrigin("*")
public class MyPerformanceController {

    @Autowired
    private MyPerformanceService performanceService;

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

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        if (roleService.isSuperAdmin(user.getWorkEmail())) return true;
        return roleService.hasPermission(user.getWorkEmail(), permission) || 
               roleService.hasPermission(user.getWorkEmail(), "employee.performance.read");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<MyPerformanceDashboardResponse>> getDashboard(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved", performanceService.getDashboard(user.getWorkEmail())));
    }

    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<MyGoalListResponse>> getGoals(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Long cycleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Goals retrieved", performanceService.getGoals(user.getWorkEmail(), cycleId, status, category, pageable)));
    }

    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<GoalDetailsResponse>> getGoalDetails(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long goalId) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Goal details retrieved", performanceService.getGoalDetails(user.getWorkEmail(), goalId)));
    }

    @PatchMapping("/goals/{goalId}/progress")
    public ResponseEntity<ApiResponse<UpdateGoalProgressResponse>> updateGoalProgress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long goalId,
            @RequestBody UpdateGoalProgressRequest req) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.goal.update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Progress updated", performanceService.updateGoalProgress(user.getWorkEmail(), goalId, req)));
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewCyclesResponse>> getReviewCycles(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Review cycles retrieved", performanceService.getReviewCycles(user.getWorkEmail())));
    }

    @PostMapping("/reviews/{reviewId}/self-assessment")
    public ResponseEntity<ApiResponse<SelfAssessmentResponse>> submitSelfAssessment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reviewId,
            @RequestBody SelfAssessmentRequest req) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.assessment.submit")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Self-assessment submitted", performanceService.submitSelfAssessment(user.getWorkEmail(), reviewId, req)));
    }

    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<FeedbackListResponse>> getFeedback(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.feedback.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Feedback retrieved", performanceService.getFeedback(user.getWorkEmail())));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<AppraisalHistoryResponse>> getHistory(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.history.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("History retrieved", performanceService.getHistory(user.getWorkEmail())));
    }

    @GetMapping("/competencies")
    public ResponseEntity<ApiResponse<CompetenciesResponse>> getCompetencies(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Competencies retrieved", performanceService.getCompetencies(user.getWorkEmail())));
    }

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<PerformanceTimelineResponse>> getTimeline(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Timeline retrieved", performanceService.getTimeline(user.getWorkEmail())));
    }

    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<PerformancePolicyResponse>> getPolicies(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Policies retrieved", performanceService.getPolicies()));
    }
}
