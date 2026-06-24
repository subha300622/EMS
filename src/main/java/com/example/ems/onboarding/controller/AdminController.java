package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.onboarding.service.OnboardingReconciliationService;
import com.example.ems.finance.service.FinanceReconciliationService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Global Administration & Self-Healing")
public class AdminController {

    @Autowired
    private OnboardingReconciliationService onboardingReconciliationService;

    @Autowired
    private FinanceReconciliationService financeReconciliationService;

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

    private boolean checkAdminPermission(User user) {
        if (user == null) return false;
        return roleService.hasRoleOrGreater(user, "HR") || roleService.hasRoleOrGreater(user, "FINANCE");
    }

    @PostMapping("/onboarding/reconcile")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> reconcileOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAdminPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Finance privileges.", "AUTH_002"));
        }
        Map<String, Object> result = onboardingReconciliationService.reconcileOnboardings();
        return ResponseEntity.ok(ApiResponse.success("Onboarding cache progress reconciled", result));
    }

    @PostMapping("/finance/reconcile")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> reconcileFinance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAdminPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Finance privileges.", "AUTH_002"));
        }
        Map<String, Object> result = financeReconciliationService.reconcileFinance();
        return ResponseEntity.ok(ApiResponse.success("Finance/Salary profiles reconciled", result));
    }
}
