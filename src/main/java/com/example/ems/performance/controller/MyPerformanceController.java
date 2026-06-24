package com.example.ems.performance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.service.MyPerformanceService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my-performance")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Performance")
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



    @Operation(summary = "Get My Performance Dashboard", description = "Retrieves stats, status, band, goals met, and self assessment ratings.")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<MyPerformanceDashboardResponse>> getDashboard(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Performance dashboard retrieved", performanceService.getDashboard(user.getWorkEmail())));
    }

    @Operation(summary = "Get Review Cycles", description = "Retrieves active performance review cycles and employee review statuses.")
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewCyclesResponse>> getReviewCycles(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Review cycles retrieved", performanceService.getReviewCycles(user.getWorkEmail())));
    }

    @Operation(summary = "Submit Self Assessment", description = "Submits employee self-assessment ratings and comments for a review cycle.")
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

    @Operation(summary = "Get Feedback", description = "Retrieves continuous feedback, peer reviews, and manager inputs for the employee.")
    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<FeedbackListResponse>> getFeedback(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.feedback.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Feedback retrieved", performanceService.getFeedback(user.getWorkEmail())));
    }

    @Operation(summary = "Get Appraisal History", description = "Retrieves past completed performance review ratings and final reports.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<AppraisalHistoryResponse>> getHistory(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.history.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("History retrieved", performanceService.getHistory(user.getWorkEmail())));
    }

    @Operation(summary = "Get Competencies", description = "Retrieves employee competency ratings and mapping profiles.")
    @GetMapping("/competencies")
    public ResponseEntity<ApiResponse<CompetenciesResponse>> getCompetencies(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Competencies retrieved", performanceService.getCompetencies(user.getWorkEmail())));
    }

    @Operation(summary = "Get Performance Timeline", description = "Retrieves the timeline of performance tasks, deadlines, and submissions.")
    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<PerformanceTimelineResponse>> getTimeline(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Timeline retrieved", performanceService.getTimeline(user.getWorkEmail())));
    }

    @Operation(summary = "Get Performance Policies", description = "Retrieves the performance appraisal guidelines and company policies.")
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<PerformancePolicyResponse>> getPolicies(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "performance.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Policies retrieved", performanceService.getPolicies()));
    }
}
