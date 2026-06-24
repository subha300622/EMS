package com.example.ems.performance.manager.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.exception.AccessDeniedException;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.performance.manager.dto.*;
import com.example.ems.performance.manager.service.ManagerPerformanceService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/performance")
@CrossOrigin("*")
@Tag(name = "Manager Performance Management")
public class ManagerPerformanceController {

    @Autowired
    private ManagerPerformanceService performanceService;

    @Autowired
    private UserRepository userRepository;

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

    @Operation(summary = "Get Team Performance Dashboard")
    @GetMapping("/team")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "cycle", defaultValue = "FY 2024-25") String cycle) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDashboardResponse response = performanceService.getTeamDashboard(currentUser.getWorkEmail(), cycle);
            return ResponseEntity.ok(ApiResponse.success("Team dashboard retrieved successfully", response));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Employee Performance Detail")
    @GetMapping("/{employeeId}/review")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getEmployeeReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestParam(value = "cycle", defaultValue = "FY 2024-25") String cycle) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ReviewDetailResponse response = performanceService.getEmployeeReview(currentUser.getWorkEmail(), employeeId,
                    cycle);
            return ResponseEntity.ok(ApiResponse.success("Review details retrieved successfully", response));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Save Manager Rating")
    @PostMapping("/{reviewId}/manager-rating")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> saveManagerRating(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId,
            @RequestBody SaveManagerRatingRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            SaveManagerRatingResponse response = performanceService.saveManagerRating(currentUser.getWorkEmail(),
                    reviewId, request);
            return ResponseEntity.ok(ApiResponse.success("Manager ratings saved successfully", response));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Submit Final Review")
    @PostMapping("/{reviewId}/submit")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> submitFinalReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            SubmitReviewResponse response = performanceService.submitFinalReview(currentUser.getWorkEmail(), reviewId);
            return ResponseEntity.ok(ApiResponse.success("Final review submitted successfully", response));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Team Summary (Top Cards Data)")
    @GetMapping("/team/summary")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "cycle", defaultValue = "FY 2024-25") String cycle) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamSummaryResponse response = performanceService.getTeamSummary(currentUser.getWorkEmail(), cycle);
            return ResponseEntity.ok(ApiResponse.success("Team summary retrieved successfully", response));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }
}
