package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance/analytics")
@CrossOrigin("*")
@Tag(name = "Finance Analytics & Reports")
public class FinanceAnalyticsController {

    @Autowired
    private EmployeeFinanceOnboardingService service;

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

    private boolean checkAccess(User user) {
        if (user == null) {
            return false;
        }
        if (roleService.hasRoleOrGreater(user, "FINANCE")) {
            return true;
        }
        return roleService.hasPermission(user.getWorkEmail(), "reports.finance")
                || roleService.hasPermission(user.getWorkEmail(), "expense.manage");
    }

    @GetMapping("/reports")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Finance onboarding report compiled", service.getReportData()));
    }

    @GetMapping("/reports/export")
    public ResponseEntity<String> exportReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false, defaultValue = "csv") String format) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String csv = service.exportReportAsCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=finance_onboarding_report.csv")
                .header("Content-Type", "text/csv")
                .body(csv);
    }

    @GetMapping("/pending-reviews")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<EmployeeFinanceOnboarding>>> getPendingReviews(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        List<EmployeeFinanceOnboarding> list = service.getPendingReviews(department, status);
        return ResponseEntity.ok(ApiResponse.success("Pending finance onboarding reviews retrieved", list));
    }

    @PostMapping("/calculate-ctc")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateCtc(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        BigDecimal monthlyCtc;
        if (body.containsKey("monthlyCtc")) {
            monthlyCtc = new BigDecimal(body.get("monthlyCtc").toString());
        } else if (body.containsKey("ctc")) {
            BigDecimal annualCtc = new BigDecimal(body.get("ctc").toString());
            monthlyCtc = annualCtc.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        } else {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Either ctc (annual) or monthlyCtc must be provided", "VAL_001"));
        }
        return ResponseEntity.ok(ApiResponse.success("CTC breakup calculated successfully", service.calculateCtcBreakup(monthlyCtc)));
    }
}
