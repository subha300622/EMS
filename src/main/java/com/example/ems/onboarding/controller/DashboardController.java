package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.onboarding.service.TeamOnboardingService;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin("*")
@Tag(name = "Centralized Dashboard Engine")
public class DashboardController {

    @Autowired
    private TeamOnboardingService teamOnboardingService;

    @Autowired
    private EmployeeFinanceOnboardingService financeOnboardingService;

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

    private boolean checkFinanceAccess(User user) {
        if (user == null) {
            return false;
        }
        if (roleService.hasRoleOrGreater(user, "FINANCE")) {
            return true;
        }
        return roleService.hasPermission(user.getWorkEmail(), "reports.finance")
                || roleService.hasPermission(user.getWorkEmail(), "expense.manage");
    }


    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false, defaultValue = "HR") String role,
            @RequestParam(required = false) Long managerId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            if ("HR".equalsIgnoreCase(role)) {
                Map<String, Object> summary = teamOnboardingService.getHrSummary();
                return ResponseEntity.ok(ApiResponse.success("HR dashboard summary metrics retrieved", summary));
            } else if ("FINANCE".equalsIgnoreCase(role)) {
                if (!checkFinanceAccess(currentUser)) {
                    return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
                }
                Map<String, Object> summary = financeOnboardingService.getDashboardSummary();
                return ResponseEntity.ok(ApiResponse.success("Finance onboarding summary counts retrieved", summary));
            } else if ("MANAGER".equalsIgnoreCase(role)) {
                // If managerId is not provided, try to resolve it from the user account or default to 1L
                Long targetManagerId = managerId;
                if (targetManagerId == null) {
                    targetManagerId = 1L; // Fallback default
                }
                Map<String, Object> stats = teamOnboardingService.getManagerDashboard(targetManagerId);
                return ResponseEntity.ok(ApiResponse.success("Manager dashboard metrics retrieved", stats));
            } else {
                return (ResponseEntity) ResponseEntity.badRequest()
                        .body(ErrorResponse.error("Unsupported role parameter: " + role, "VAL_002"));
            }
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        }
    }
}
