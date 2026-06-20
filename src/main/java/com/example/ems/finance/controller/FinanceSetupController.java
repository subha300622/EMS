package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.entity.FinanceOnboarding;
import com.example.ems.finance.service.FinanceOnboardingService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/finance/setup")
@CrossOrigin("*")
@Tag(name = "Finance Setup")
public class FinanceSetupController {

    @Autowired
    private FinanceOnboardingService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

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

    private boolean checkAccess(User user) {
        if (user == null) {
            return false;
        }
        if (roleService.hasRoleOrGreater(user, "FINANCE")) {
            return true;
        }
        return roleService.hasPermission(user.getWorkEmail(), "reports.finance") || roleService.hasPermission(user.getWorkEmail(), "expense.manage");
    }

    // ── 1. CREATE SETUP ────────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Create Finance Setup", description = "Creates and persists a new finance setup cycle with the provided payload.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> createSetup(@RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        FinanceOnboarding onboarding = service.createOnboarding();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Finance setup process started successfully", onboarding));
    }

    // ── 2. RESUME CURRENT SETUP ────────────────────────────────────────
    @GetMapping("/current")
    @Operation(summary = "Get Current Finance Setup", description = "Retrieves details of current active finance setup session from the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Retrieved successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getCurrentSetup(@RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        Optional<FinanceOnboarding> currentOpt = service.getCurrentOnboarding();
        if (currentOpt.isPresent()) {
            return (ResponseEntity) ResponseEntity.ok(ApiResponse.success("Current finance setup retrieved", currentOpt.get()));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("No active setup session found", "OB_001"));
        }
    }

    // ── 3. GET DETAILS ──────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get Setup Details", description = "Retrieves details of specific setup process by ID from the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Retrieved successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSetupDetails(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        Optional<FinanceOnboarding> setupOpt = service.getOnboardingById(id);
        if (setupOpt.isPresent()) {
            return (ResponseEntity) ResponseEntity.ok(ApiResponse.success("Finance setup details retrieved", setupOpt.get()));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Setup session not found with ID: " + id, "OB_002"));
        }
    }

    // ── 4. GET PROGRESS ─────────────────────────────────────────────────────
    @GetMapping("/{id}/progress")
    @Operation(summary = "Get Setup Progress", description = "Retrieves current completion progress percentage of finance setup from the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Retrieved successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSetupProgress(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        Optional<FinanceOnboarding> setupOpt = service.getOnboardingById(id);
        if (setupOpt.isPresent()) {
            FinanceOnboarding ob = setupOpt.get();
            int pct = service.calculateProgress(ob);
            return (ResponseEntity) ResponseEntity.ok(ApiResponse.success("Finance setup progress calculated", Map.of("id", id, "progressPercentage", pct, "status", ob.getStatus())));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Setup session not found with ID: " + id, "OB_002"));
        }
    }

    // ── 5. STEP PATCHES ─────────────────────────────────────────────────────
    @PatchMapping("/{id}/company")
    @Operation(summary = "Update Company", description = "Performs a partial update or executes a specific state transition for company.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> updateCompany(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id, @RequestBody Map<String, String> data){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            FinanceOnboarding updated = service.updateCompany(id, data);
            return ResponseEntity.ok(ApiResponse.success("Company settings step updated", updated));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/bank-account")
    @Operation(summary = "Update Bank Account", description = "Performs a partial update or executes a specific state transition for bank account.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> updateBankAccount(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id, @RequestBody Map<String, String> data){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            FinanceOnboarding updated = service.updateBankAccount(id, data);
            return ResponseEntity.ok(ApiResponse.success("Bank account settings step updated", updated));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/tax")
    @Operation(summary = "Update Tax", description = "Performs a partial update or executes a specific state transition for tax.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> updateTax(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id, @RequestBody Map<String, Object> data){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            FinanceOnboarding updated = service.updateTax(id, data);
            return ResponseEntity.ok(ApiResponse.success("Tax settings step updated", updated));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/payment-method")
    @Operation(summary = "Update Payment Method", description = "Performs a partial update or executes a specific state transition for payment method.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> updatePaymentMethod(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id, @RequestBody Map<String, String> data){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            FinanceOnboarding updated = service.updatePaymentMethod(id, data);
            return ResponseEntity.ok(ApiResponse.success("Payment method settings step updated", updated));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/payroll")
    @Operation(summary = "Update Payroll", description = "Performs a partial update or executes a specific state transition for payroll.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> updatePayroll(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id, @RequestBody Map<String, Integer> data){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            FinanceOnboarding updated = service.updatePayroll(id, data);
            return ResponseEntity.ok(ApiResponse.success("Payroll settings step updated", updated));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/budget")
    @Operation(summary = "Update Budget", description = "Performs a partial update or executes a specific state transition for budget.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> updateBudget(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id, @RequestBody Map<String, Object> data){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            FinanceOnboarding updated = service.updateBudget(id, data);
            return ResponseEntity.ok(ApiResponse.success("Budget settings step updated", updated));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    // ── 6. VALIDATE SETUP ──────────────────────────────────────────────
    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate Finance Setup", description = "Creates and persists a new validation check for finance setup details.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Void>> validateSetup(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            List<String> errors = service.validateOnboarding(id);
            if (errors.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Finance setup successfully validated. All configurations are correct."));
            } else {
                return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Validation failed: " + String.join(", ", errors), "OB_004"));
            }
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OB_002"));
        }
    }

    // ── 7. COMPLETE SETUP ──────────────────────────────────────────────
    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete Finance Setup", description = "Creates and persists the finalized settings for the company's financial operations.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FinanceOnboarding>> completeSetup(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance setup privileges.", "AUTH_002"));
        }
        try {
            FinanceOnboarding completed = service.completeOnboarding(id);
            return ResponseEntity.ok(ApiResponse.success("Finance setup completed and system-wide settings updated successfully", completed));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_004"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OB_002"));
        }
    }

    // ── 8. ADMIN LIST & ARCHIVE ─────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "List Setup Records", description = "Retrieves details of list setup records from the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Retrieved successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<FinanceOnboarding>>> listSetupRecords(@RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires admin/finance privileges.", "AUTH_002"));
        }
        List<FinanceOnboarding> list = service.listAll();
        return ResponseEntity.ok(ApiResponse.success("All finance setup records retrieved successfully", list));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive Setup", description = "Deletes the specified archive setup permanently from the database.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({ @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden") })
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Void>> archiveSetup(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires admin/finance privileges.", "AUTH_002"));
        }
        boolean archived = service.archiveOnboarding(id);
        if (archived) {
            return ResponseEntity.ok(ApiResponse.success("Finance setup record archived successfully"));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Setup session not found with ID: " + id, "OB_002"));
        }
    }
}
