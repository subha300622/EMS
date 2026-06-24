package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/approvals")
@CrossOrigin("*")
@Tag(name = "Centralized Approvals Engine")
public class ApprovalController {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private EmployeeFinanceOnboardingService financeOnboardingService;

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

    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> handleApproval(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String entityType = (String) body.get("entityType");
        Number entityIdNum = (Number) body.get("entityId");
        String action = (String) body.get("action");
        String notes = (String) body.getOrDefault("notes", "Action processed by approvals engine");

        if (entityType == null || entityIdNum == null || action == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("entityType, entityId, and action are required", "VAL_001"));
        }

        Long entityId = entityIdNum.longValue();

        try {
            if ("ONBOARDING".equalsIgnoreCase(entityType)) {
                if ("APPROVE".equalsIgnoreCase(action)) {
                    return onboardingService.approveOnboarding(entityId)
                            .map(res -> ResponseEntity.ok(ApiResponse.success("Onboarding approved successfully", (Object) res)))
                            .orElseGet(() -> (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(ErrorResponse.error("Onboarding not found", "ONB_002")));
                } else if ("COMPLETE".equalsIgnoreCase(action)) {
                    return onboardingService.completeOnboarding(entityId)
                            .map(res -> ResponseEntity.ok(ApiResponse.success("Onboarding completed successfully", (Object) res)))
                            .orElseGet(() -> (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(ErrorResponse.error("Onboarding not found", "ONB_002")));
                } else {
                    return (ResponseEntity) ResponseEntity.badRequest()
                            .body(ErrorResponse.error("Unsupported action for ONBOARDING entity", "VAL_002"));
                }
            } else if ("FINANCE".equalsIgnoreCase(entityType)) {
                if ("APPROVE".equalsIgnoreCase(action)) {
                    Object res = financeOnboardingService.approve(entityId, currentUser.getWorkEmail(), notes);
                    return ResponseEntity.ok(ApiResponse.success("Finance onboarding approved successfully", res));
                } else if ("REJECT".equalsIgnoreCase(action)) {
                    Object res = financeOnboardingService.reject(entityId, currentUser.getWorkEmail(), notes);
                    return ResponseEntity.ok(ApiResponse.success("Finance onboarding rejected successfully", res));
                } else if ("SEND_BACK".equalsIgnoreCase(action)) {
                    Object res = financeOnboardingService.sendBack(entityId, currentUser.getWorkEmail(), notes);
                    return ResponseEntity.ok(ApiResponse.success("Finance onboarding sent back for correction", res));
                } else {
                    return (ResponseEntity) ResponseEntity.badRequest()
                            .body(ErrorResponse.error("Unsupported action for FINANCE entity", "VAL_002"));
                }
            } else {
                return (ResponseEntity) ResponseEntity.badRequest()
                        .body(ErrorResponse.error("Unsupported entityType: " + entityType, "VAL_002"));
            }
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        }
    }

    @GetMapping("/onboarding/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOnboardingHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        List<Map<String, Object>> timeline = onboardingService.getOnboardingTimeline(id);
        return ResponseEntity.ok(ApiResponse.success("Onboarding approvals history timeline retrieved", timeline));
    }

    @GetMapping("/finance/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<?>>> getFinanceHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        List<?> history = financeOnboardingService.getHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Finance approvals history logs retrieved", history));
    }
}
