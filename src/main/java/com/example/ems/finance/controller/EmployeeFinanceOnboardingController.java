package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.entity.FinanceOnboardingHistory;
import com.example.ems.finance.dto.FinanceCommandEnvelope;
import com.example.ems.finance.repository.EmployeeFinanceOnboardingRepository;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.finance.validation.FinanceCommandValidator;
import com.example.ems.finance.handler.FinanceCommandRouter;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/onboarding")
@CrossOrigin("*")
@Tag(name = "Finance Onboarding")
public class EmployeeFinanceOnboardingController {

    @Autowired
    private EmployeeFinanceOnboardingService service;

    @Autowired
    private EmployeeFinanceOnboardingRepository repository;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private FinanceCommandValidator validator;

    @Autowired
    private FinanceCommandRouter router;

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

    private Long resolveEmployeeIdFromOnboardingId(Long onboardingId) {
        return onboardingRepository.findById(onboardingId)
                .map(ob -> ob.getEmployee().getId())
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + onboardingId));
    }

    private Long resolveFinanceOnboardingIdFromOnboardingId(Long onboardingId) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + onboardingId));
        Employee employee = onboarding.getEmployee();

        return repository.findByEmployeeId(employee.getId())
                .orElseGet(() -> {
                    EmployeeFinanceOnboarding ob = new EmployeeFinanceOnboarding();
                    ob.setEmployee(employee);
                    ob.setStatus("DRAFT");
                    return repository.save(ob);
                }).getId();
    }

    // ── 1. GET DETAILS (BY CORE ONBOARDING ID) ───────────────────────────────
    @GetMapping("/{id}/finance")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> getDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long financeOnboardingId = resolveFinanceOnboardingIdFromOnboardingId(id);
            return service.get(financeOnboardingId)
                    .map(ob -> ResponseEntity.ok(ApiResponse.success("Finance onboarding details retrieved", ob)))
                    .orElseGet(() -> (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Onboarding session not found", "ONB_002")));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        }
    }

    // ── 2. COMMAND DISPATCH ENDPOINT (MUTATIONS) ─────────────────────────────
    @PostMapping("/{id}/finance/command")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> handleFinanceCommand(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody FinanceCommandEnvelope envelope) {
        
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            // Validate incoming command envelope
            validator.validate(envelope);

            // Execute via Command Router
            EmployeeFinanceOnboarding result = router.route(id, envelope, user.getWorkEmail(), idempotencyKey);
            return ResponseEntity.ok(ApiResponse.success("Command processed successfully", result));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate command")) {
                return ResponseEntity.ok(ApiResponse.success("Request processed successfully (cached response)"));
            }
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        }
    }

    // ── 3. BANK DETAILS ──────────────────────────────────────────────────────
    @GetMapping("/{id}/finance/bank-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBankDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long employeeId = resolveEmployeeIdFromOnboardingId(id);
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("bankName", ob.getBankName());
            map.put("bankAccountNumber", ob.getBankAccountNumber());
            map.put("bankIfsc", ob.getBankIfsc());
            map.put("bankVerificationStatus", ob.getBankVerificationStatus());
            map.put("bankVerificationNotes", ob.getBankVerificationNotes());
            return ResponseEntity.ok(ApiResponse.success("Bank details retrieved", map));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 4. TAX DETAILS ───────────────────────────────────────────────────────
    @GetMapping("/{id}/finance/tax-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaxDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long employeeId = resolveEmployeeIdFromOnboardingId(id);
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("panNumber", ob.getPanNumber());
            map.put("panVerificationStatus", ob.getPanVerificationStatus());
            map.put("panVerificationNotes", ob.getPanVerificationNotes());
            return ResponseEntity.ok(ApiResponse.success("Tax details retrieved", map));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 5. STATUTORY DETAILS ─────────────────────────────────────────────────
    @GetMapping("/{id}/finance/statutory-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatutoryDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long employeeId = resolveEmployeeIdFromOnboardingId(id);
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("uanNumber", ob.getUanNumber());
            map.put("uanVerificationStatus", ob.getUanVerificationStatus());
            map.put("uanVerificationNotes", ob.getUanVerificationNotes());
            return ResponseEntity.ok(ApiResponse.success("Statutory details retrieved", map));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 6. SALARY DETAILS ────────────────────────────────────────────────────
    @GetMapping("/{id}/finance/salary-structure")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalaryStructureForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long employeeId = resolveEmployeeIdFromOnboardingId(id);
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Assigned salary structure retrieved", service.getSalaryStructure(ob.getId())));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{id}/finance/salary-preview")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalaryPreviewForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long employeeId = resolveEmployeeIdFromOnboardingId(id);
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Salary breakup and preview generated", service.getSalaryPreview(ob.getId())));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 7. PAYROLL STATUS ────────────────────────────────────────────────────
    @GetMapping("/{id}/finance/payroll-status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollStatusForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long employeeId = resolveEmployeeIdFromOnboardingId(id);
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Payroll activation status checked", service.getPayrollStatus(ob.getId())));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 8. GENERAL FINANCE SERVICES ──────────────────────────────────────────
    @GetMapping("/{id}/finance/verification-status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVerificationStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long financeOnboardingId = resolveFinanceOnboardingIdFromOnboardingId(id);
            return ResponseEntity.ok(ApiResponse.success("Verification check statuses retrieved", service.getVerificationStatus(financeOnboardingId)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{id}/finance/history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<FinanceOnboardingHistory>>> getFinanceHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            Long financeOnboardingId = resolveFinanceOnboardingIdFromOnboardingId(id);
            List<FinanceOnboardingHistory> history = service.getHistory(financeOnboardingId);
            return ResponseEntity.ok(ApiResponse.success("Approval and verification action logs retrieved", history));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        }
    }
}
