package com.example.ems.leave.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.*;
import com.example.ems.leave.entity.*;
import com.example.ems.leave.service.*;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/leave", "/api/v1/leaves", "/api/v1/leave-types", "/api/v1/manager"})
@CrossOrigin("*")
@Tag(name = "Leave Management", description = "Complete Leave Management APIs")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private LeaveAccrualService leaveAccrualService;

    @Autowired
    private LeaveEncashmentService leaveEncashmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    private Employee resolveEmployee(User user) {
        if (user == null || user.getWorkEmail() == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private Long resolveOrgId(User user) {
        if (user != null && user.getOrganization() != null) return user.getOrganization().getId();
        Employee emp = resolveEmployee(user);
        if (emp != null && emp.getOrganization() != null) return emp.getOrganization().getId();
        return 1L;
    }

    // == 1. LEAVE TYPES ========================================================

    @Operation(summary = "Create Leave Type")
    @PostMapping("/types")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LeaveTypeRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee admin = resolveEmployee(user);
        LeaveType type = leaveService.createLeaveType(admin, request);
        return ResponseEntity.ok(ApiResponse.success("Leave type created successfully", type));
    }

    @Operation(summary = "List Leave Types")
    @GetMapping("/types")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveTypes(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveType> list = leaveService.getAllLeaveTypes(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Leave types retrieved successfully", list));
    }

    @Operation(summary = "Get Leave Type Details")
    @GetMapping("/types/{leaveTypeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveTypeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveType type = leaveService.getLeaveTypeById(leaveTypeId);
        return ResponseEntity.ok(ApiResponse.success("Leave type details retrieved successfully", type));
    }

    @Operation(summary = "Edit Leave Type")
    @PutMapping("/types/{leaveTypeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveTypeId,
            @RequestBody LeaveTypeRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveType type = leaveService.updateLeaveType(leaveTypeId, request);
        return ResponseEntity.ok(ApiResponse.success("Leave type updated successfully", type));
    }

    @Operation(summary = "Soft Delete Leave Type")
    @DeleteMapping("/types/{leaveTypeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveTypeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        leaveService.deleteLeaveType(leaveTypeId);
        return ResponseEntity.ok(ApiResponse.success("Leave type deleted successfully", null));
    }

    @Operation(summary = "Activate/Deactivate Leave Type")
    @PatchMapping({"/types/{leaveTypeId}/status", "/types/{leaveTypeId}/deactivate", "/{leaveTypeId}/deactivate"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> toggleLeaveTypeStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveTypeId,
            @RequestParam(required = false, defaultValue = "false") boolean active) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveType type = leaveService.toggleLeaveTypeStatus(leaveTypeId, active);
        return ResponseEntity.ok(ApiResponse.success("Leave type status updated successfully", type));
    }

    @PatchMapping({"/types/{leaveTypeId}/activate", "/{leaveTypeId}/activate"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> activateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveTypeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveType type = leaveService.activateLeaveType(leaveTypeId);
        return ResponseEntity.ok(ApiResponse.success("Leave type activated successfully", type));
    }

    // == 2. LEAVE POLICIES =====================================================

    @Operation(summary = "Create Policy")
    @PostMapping("/policies")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createPolicy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LeavePolicyRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee admin = resolveEmployee(user);
        LeavePolicy policy = leaveService.createLeavePolicy(admin, request);
        return ResponseEntity.ok(ApiResponse.success("Leave policy created successfully", policy));
    }

    @Operation(summary = "List Policies")
    @GetMapping("/policies")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPolicies(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeavePolicy> list = leaveService.getAllLeavePolicies(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Leave policies retrieved successfully", list));
    }

    @Operation(summary = "Get Policy Details")
    @GetMapping("/policies/{policyId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPolicy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeavePolicy policy = leaveService.getLeavePolicyById(policyId);
        return ResponseEntity.ok(ApiResponse.success("Leave policy details retrieved successfully", policy));
    }

    @Operation(summary = "Edit Policy")
    @PutMapping("/policies/{policyId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updatePolicy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId,
            @RequestBody LeavePolicyRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeavePolicy policy = leaveService.updateLeavePolicy(policyId, request);
        return ResponseEntity.ok(ApiResponse.success("Leave policy updated successfully", policy));
    }

    @Operation(summary = "Soft Delete Policy")
    @DeleteMapping("/policies/{policyId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deletePolicy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        leaveService.deleteLeavePolicy(policyId);
        return ResponseEntity.ok(ApiResponse.success("Leave policy deleted successfully", null));
    }

    @Operation(summary = "Activate/Deactivate Policy")
    @PatchMapping("/policies/{policyId}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> togglePolicyStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeavePolicy policy = leaveService.toggleLeavePolicyStatus(policyId, status);
        return ResponseEntity.ok(ApiResponse.success("Leave policy status updated successfully", policy));
    }

    // == 3. LEAVE RULES ========================================================

    @Operation(summary = "Create Leave Rule")
    @PostMapping("/rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateLeaveRuleRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee admin = resolveEmployee(user);
        LeaveRule rule = leaveService.createLeaveRule(admin, request);
        return ResponseEntity.ok(ApiResponse.success("Leave rule created successfully", rule));
    }

    @Operation(summary = "List Rules")
    @GetMapping("/rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getRules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveRule> list = leaveService.getAllLeaveRules(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Leave rules retrieved successfully", list));
    }

    @Operation(summary = "Rule Details")
    @GetMapping("/rules/{ruleId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long ruleId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveRule rule = leaveService.getLeaveRuleById(ruleId);
        return ResponseEntity.ok(ApiResponse.success("Leave rule details retrieved successfully", rule));
    }

    @Operation(summary = "Edit Rule")
    @PutMapping("/rules/{ruleId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long ruleId,
            @RequestBody CreateLeaveRuleRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveRule rule = leaveService.updateLeaveRule(ruleId, request);
        return ResponseEntity.ok(ApiResponse.success("Leave rule updated successfully", rule));
    }

    @Operation(summary = "Delete Rule")
    @DeleteMapping("/rules/{ruleId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long ruleId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        leaveService.deleteLeaveRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success("Leave rule deleted successfully", null));
    }

    // == 4. ACCRUAL RULES ======================================================

    @Operation(summary = "Create Accrual Rule")
    @PostMapping("/accrual-rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createAccrualRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateAccrualRuleRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee admin = resolveEmployee(user);
        LeaveAccrualRule rule = leaveService.createAccrualRule(admin, request);
        return ResponseEntity.ok(ApiResponse.success("Accrual rule created successfully", rule));
    }

    @Operation(summary = "List Accrual Rules")
    @GetMapping("/accrual-rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAccrualRules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveAccrualRule> list = leaveService.getAllAccrualRules(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Accrual rules retrieved successfully", list));
    }

    @Operation(summary = "Accrual Rule Details")
    @GetMapping("/accrual-rules/{accrualRuleId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAccrualRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long accrualRuleId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveAccrualRule rule = leaveService.getAccrualRuleById(accrualRuleId);
        return ResponseEntity.ok(ApiResponse.success("Accrual rule details retrieved successfully", rule));
    }

    @Operation(summary = "Edit Accrual Rule")
    @PutMapping("/accrual-rules/{accrualRuleId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateAccrualRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long accrualRuleId,
            @RequestBody CreateAccrualRuleRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveAccrualRule rule = leaveService.updateAccrualRule(accrualRuleId, request);
        return ResponseEntity.ok(ApiResponse.success("Accrual rule updated successfully", rule));
    }

    @Operation(summary = "Disable Accrual Rule")
    @DeleteMapping("/accrual-rules/{accrualRuleId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteAccrualRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long accrualRuleId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        leaveService.deleteAccrualRule(accrualRuleId);
        return ResponseEntity.ok(ApiResponse.success("Accrual rule disabled successfully", null));
    }

    // == 5. LEAVE BALANCE & BALANCE ADJUSTMENTS ================================

    @Operation(summary = "List Employee Balances")
    @GetMapping("/balances")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getBalances(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveBalance> list = leaveBalanceService.getOrganizationBalances(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Leave balances retrieved successfully", list));
    }

    @Operation(summary = "My Leave Balance")
    @GetMapping("/balances/me")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyBalances(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee emp = resolveEmployee(user);
        if (emp == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.error("Employee profile not found", "EMP_002"));

        List<LeaveBalance> list = leaveBalanceService.getEmployeeBalances(emp.getId(), LocalDate.now().getYear());
        return ResponseEntity.ok(ApiResponse.success("My leave balances retrieved successfully", list));
    }

    @Operation(summary = "Employee Leave Balance")
    @GetMapping("/balances/{employeeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeBalances(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveBalance> list = leaveBalanceService.getEmployeeBalances(employeeId, LocalDate.now().getYear());
        return ResponseEntity.ok(ApiResponse.success("Employee leave balances retrieved successfully", list));
    }

    @Operation(summary = "Specific Leave Type Balance")
    @GetMapping("/balances/{employeeId}/{leaveTypeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeSpecificBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @PathVariable Long leaveTypeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveType lt = leaveService.getLeaveTypeById(leaveTypeId);
        Employee emp = employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        LeaveBalance balance = leaveBalanceService.getOrCreateBalance(emp, lt, LocalDate.now().getYear());

        return ResponseEntity.ok(ApiResponse.success("Specific leave balance retrieved successfully", balance));
    }

    @Operation(summary = "Add/Subtract Balance (Balance Adjustment)")
    @PostMapping("/balance-adjustments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> adjustBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody BalanceAdjustmentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveBalanceAdjustment adj = leaveBalanceService.adjustBalance(user, request);
        return ResponseEntity.ok(ApiResponse.success("Balance adjusted successfully", adj));
    }

    @Operation(summary = "List Adjustments")
    @GetMapping("/balance-adjustments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAdjustments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveBalanceAdjustment> list = leaveBalanceService.getAdjustments(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Balance adjustments retrieved successfully", list));
    }

    // == 6. ACCRUAL OPERATIONS ================================================

    @Operation(summary = "Run Accrual Operation")
    @PostMapping("/accruals/run")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> runAccrual(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveAccrualTransaction> txns = leaveAccrualService.runAccrualsForOrganization(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Accrual operation executed successfully", txns));
    }

    @Operation(summary = "Accrue for Employee")
    @PostMapping("/accruals/employee/{employeeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> accrueForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveAccrualTransaction> txns = leaveAccrualService.accrueForEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee accrual executed successfully", txns));
    }

    @Operation(summary = "Accrual History")
    @GetMapping("/accruals")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAccrualHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveAccrualTransaction> list = leaveAccrualService.getAccrualHistory(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Accrual history retrieved successfully", list));
    }

    // == 7. ENCASHMENTS =======================================================

    @Operation(summary = "Request Encashment")
    @PostMapping("/encashments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> requestEncashment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateEncashmentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee emp = resolveEmployee(user);
        LeaveEncashment enc = leaveEncashmentService.requestEncashment(emp, request);
        return ResponseEntity.ok(ApiResponse.success("Encashment request submitted successfully", enc));
    }

    @Operation(summary = "List Encashments")
    @GetMapping("/encashments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEncashments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveEncashment> list = leaveEncashmentService.getEncashments(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Encashments retrieved successfully", list));
    }

    @Operation(summary = "Encashment Details")
    @GetMapping("/encashments/{encashmentId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEncashment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long encashmentId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveEncashment enc = leaveEncashmentService.getEncashmentById(encashmentId);
        return ResponseEntity.ok(ApiResponse.success("Encashment details retrieved successfully", enc));
    }

    @Operation(summary = "Approve Encashment")
    @PatchMapping("/encashments/{encashmentId}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveEncashment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long encashmentId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveEncashment enc = leaveEncashmentService.approveEncashment(encashmentId);
        return ResponseEntity.ok(ApiResponse.success("Encashment approved successfully", enc));
    }

    @Operation(summary = "Reject Encashment")
    @PatchMapping("/encashments/{encashmentId}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectEncashment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long encashmentId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveEncashment enc = leaveEncashmentService.rejectEncashment(encashmentId);
        return ResponseEntity.ok(ApiResponse.success("Encashment rejected successfully", enc));
    }

    @Operation(summary = "Cancel Encashment")
    @PatchMapping("/encashments/{encashmentId}/cancel")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> cancelEncashment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long encashmentId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveEncashment enc = leaveEncashmentService.cancelEncashment(encashmentId);
        return ResponseEntity.ok(ApiResponse.success("Encashment cancelled successfully", enc));
    }

    // == 8. LEAVE REQUESTS ====================================================

    @Operation(summary = "Apply Leave")
    @PostMapping({"", "/requests"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> applyLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid LeaveRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee emp = resolveEmployee(user);
        if (emp == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.error("Employee profile not found", "EMP_002"));

        try {
            Leave record = leaveService.applyLeave(emp, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Leave request submitted successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "My Leave Requests")
    @GetMapping("/my-requests")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyLeaveRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee emp = resolveEmployee(user);
        List<Leave> myLeaves = emp != null ? leaveService.getLeavesByEmployeeId(emp.getId()) : List.of();
        return ResponseEntity.ok(ApiResponse.success("My leave requests retrieved successfully", myLeaves));
    }

    @Operation(summary = "Leave List")
    @GetMapping({"", "/requests"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean my) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        if (Boolean.TRUE.equals(my)) {
            Employee emp = resolveEmployee(user);
            List<Leave> myLeaves = emp != null ? leaveService.getLeavesByEmployeeId(emp.getId()) : List.of();
            return ResponseEntity.ok(ApiResponse.success("Leave history retrieved successfully", myLeaves));
        }

        List<Leave> list = leaveService.getLeaves(resolveOrgId(user), employeeId, leaveTypeId, status, fromDate, toDate, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", list));
    }

    @Operation(summary = "Leave Details")
    @GetMapping("/requests/{leaveRequestId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Leave leave = leaveService.getLeaveById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveRequestId));
        return ResponseEntity.ok(ApiResponse.success("Leave request details retrieved successfully", leave));
    }

    @Operation(summary = "Edit Leave Request")
    @PutMapping("/requests/{leaveRequestId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateLeaveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId,
            @RequestBody @Valid LeaveRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee emp = resolveEmployee(user);
        Leave leave = leaveService.updateLeave(leaveRequestId, emp, request);
        return ResponseEntity.ok(ApiResponse.success("Leave request updated successfully", leave));
    }

    @Operation(summary = "Cancel Leave Request")
    @PatchMapping("/requests/{leaveRequestId}/cancel")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> cancelLeaveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee actor = resolveEmployee(user);
        Leave leave = leaveService.cancelLeave(leaveRequestId, actor);
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled successfully", leave));
    }

    @Operation(summary = "Leave History Audit")
    @GetMapping("/requests/{leaveRequestId}/history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveRequestHistory> history = leaveService.getLeaveHistory(leaveRequestId);
        return ResponseEntity.ok(ApiResponse.success("Leave request history retrieved successfully", history));
    }

    @PatchMapping({"/{leaveId}/approve", "/requests/{leaveId}/approve"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        Leave leave = leaveService.approveLeave(leaveId, approver);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", leave));
    }

    @PatchMapping({"/{leaveId}/reject", "/requests/{leaveId}/reject"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        Leave leave = leaveService.rejectLeave(leaveId, approver);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", leave));
    }

    @GetMapping({"leave-approvals", "leave-approvals"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getManagerLeaveApprovals(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee manager = resolveEmployee(user);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page != null ? page : 0, size != null ? size : 10);
        org.springframework.data.domain.Page<LeaveApprovalResponseDto> approvals = leaveService.getManagerLeaveApprovals(manager, status, employeeId, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Manager leave approvals retrieved successfully", approvals));
    }

    @GetMapping({"leave-approvals/summary", "/summary"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveApprovalSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee manager = resolveEmployee(user);
        LeaveApprovalSummaryDto summary = leaveService.getLeaveApprovalSummary(manager);
        return ResponseEntity.ok(ApiResponse.success("Leave approval summary retrieved successfully", summary));
    }

    @GetMapping({"leave-approvals/{leaveId}", "/manager/approvals/{leaveId}", "/{leaveId:[0-9]+}"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getManagerLeaveApprovalDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee manager = resolveEmployee(user);
        LeaveApprovalResponseDto details = leaveService.getManagerLeaveApprovalDetails(leaveId, manager);
        return ResponseEntity.ok(ApiResponse.success("Manager leave approval details retrieved successfully", details));
    }

    @PostMapping({"leave-approvals/{leaveId}/approve", "/{leaveId:[0-9]+}/approve"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveLeaveWithComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveId,
            @RequestBody(required = false) ManagerCommentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        String comment = request != null ? request.getComment() : null;
        ManagerApprovalActionResponseDto result = leaveService.approveLeaveWithComment(leaveId, comment, approver);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", result));
    }

    @PostMapping({"leave-approvals/{leaveId}/reject", "/{leaveId:[0-9]+}/reject"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectLeaveWithComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveId,
            @RequestBody(required = false) ManagerCommentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        String comment = request != null ? request.getComment() : null;
        ManagerApprovalActionResponseDto result = leaveService.rejectLeaveWithComment(leaveId, comment, approver);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", result));
    }

    @PostMapping({"leave-approvals/bulk-approve", "/bulk-approve"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> bulkApproveLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody BulkApprovalRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        if (request != null && request.getLeaveIds() != null) {
            for (Long id : request.getLeaveIds()) {
                leaveService.approveLeaveWithComment(id, request.getComment(), approver);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk approval successful", null));
    }

    @PostMapping({"leave-approvals/bulk-reject", "/bulk-reject"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> bulkRejectLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody BulkApprovalRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        if (request != null && request.getLeaveIds() != null) {
            for (Long id : request.getLeaveIds()) {
                leaveService.rejectLeaveWithComment(id, request.getComment(), approver);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk rejection successful", null));
    }

    // == 9. LEAVE CALENDAR ====================================================

    @Operation(summary = "Organization Calendar")
    @GetMapping("/calendar")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<Leave> calendar = leaveService.getOrganizationCalendar(resolveOrgId(user), fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Organization leave calendar retrieved successfully", calendar));
    }

    @Operation(summary = "My Calendar")
    @GetMapping("/calendar/me")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee emp = resolveEmployee(user);
        List<Leave> calendar = leaveService.getEmployeeCalendar(emp.getId(), fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("My leave calendar retrieved successfully", calendar));
    }

    @Operation(summary = "Employee Calendar")
    @GetMapping("/calendar/employee/{employeeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<Leave> calendar = leaveService.getEmployeeCalendar(employeeId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Employee leave calendar retrieved successfully", calendar));
    }

    // == 10. SCOPE APIS =======================================================

    @Operation(summary = "Employee Requests Scope")
    @GetMapping("/employees/{employeeId}/requests")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<Leave> list = leaveService.getLeaves(resolveOrgId(user), employeeId, null, null, null, null, null);
        return ResponseEntity.ok(ApiResponse.success("Employee leave requests retrieved successfully", list));
    }

    @Operation(summary = "Employee Balance Scope")
    @GetMapping("/employees/{employeeId}/balance")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeBalanceScope(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveBalance> balances = leaveBalanceService.getEmployeeBalances(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee leave balances retrieved successfully", balances));
    }

    @Operation(summary = "Organization Requests Scope")
    @GetMapping("/organization/requests")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOrgRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<Leave> list = leaveService.getLeaves(resolveOrgId(user), null, null, null, null, null, null);
        return ResponseEntity.ok(ApiResponse.success("Organization leave requests retrieved successfully", list));
    }

    @Operation(summary = "Organization Balances Scope")
    @GetMapping("/organization/balances")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOrgBalances(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveBalance> list = leaveBalanceService.getOrganizationBalances(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Organization leave balances retrieved successfully", list));
    }
}
// End of LeaveController
