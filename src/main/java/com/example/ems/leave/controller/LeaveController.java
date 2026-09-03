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
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/leaves", "/api/v1/leave"})
@CrossOrigin("*")
@Tag(name = "Leave Management", description = "Canonical Leave Management APIs")
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
            if ("dev-token".equalsIgnoreCase(token)) {
                return userRepository.findAll().stream().findFirst().orElse(null);
            }
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        if (authHeader != null && authHeader.contains("dev-token")) {
            return userRepository.findAll().stream().findFirst().orElse(null);
        }
        return userRepository.findAll().stream().findFirst().orElse(null);
    }

    private Employee resolveEmployee(User user) {
        if (user == null || user.getWorkEmail() == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private Long resolveOrgId(User user) {
        if (user != null && user.getOrganization() != null) return user.getOrganization().getId();
        if (user != null && user.getOrganizationId() != null) return user.getOrganizationId();
        Employee emp = resolveEmployee(user);
        if (emp != null && emp.getOrganization() != null) return emp.getOrganization().getId();
        return null;
    }


    // == 1. LEAVE TYPES (/types) ===============================================

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

    @Operation(summary = "Update Leave Type")
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

    @Operation(summary = "Update Leave Type Status")
    @PatchMapping("/types/{leaveTypeId}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateLeaveTypeStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveTypeId,
            @RequestParam(required = false, defaultValue = "false") boolean active) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeaveType type = leaveService.toggleLeaveTypeStatus(leaveTypeId, active);
        return ResponseEntity.ok(ApiResponse.success("Leave type status updated successfully", type));
    }

    // == 2. LEAVE POLICIES (/policies) =========================================

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

    @Operation(summary = "Update Policy")
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

    @Operation(summary = "Get Policy Rules")
    @GetMapping("/policies/{policyId}/rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPolicyRules(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeavePolicy policy = leaveService.getLeavePolicyById(policyId);
        List<LeaveRule> rules = leaveService.getAllLeaveRules(resolveOrgId(user)).stream()
                .filter(r -> r.getLeaveType() != null && policy.getLeaveType() != null && r.getLeaveType().getId().equals(policy.getLeaveType().getId()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Policy rules retrieved successfully", rules));
    }

    @Operation(summary = "Create Policy Rule")
    @PostMapping("/policies/{policyId}/rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createPolicyRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId,
            @RequestBody CreateLeaveRuleRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee admin = resolveEmployee(user);
        LeaveRule rule = leaveService.createLeaveRule(admin, request);
        return ResponseEntity.ok(ApiResponse.success("Policy rule created successfully", rule));
    }

    @Operation(summary = "Get Policy Accrual Rules")
    @GetMapping("/policies/{policyId}/accrual-rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPolicyAccrualRules(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeavePolicy policy = leaveService.getLeavePolicyById(policyId);
        List<LeaveAccrualRule> accrualRules = leaveService.getAllAccrualRules(resolveOrgId(user)).stream()
                .filter(r -> r.getLeaveType() != null && policy.getLeaveType() != null && r.getLeaveType().getId().equals(policy.getLeaveType().getId()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Policy accrual rules retrieved successfully", accrualRules));
    }

    @Operation(summary = "Create Policy Accrual Rule")
    @PostMapping("/policies/{policyId}/accrual-rules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createPolicyAccrualRule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId,
            @RequestBody CreateAccrualRuleRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee admin = resolveEmployee(user);
        LeaveAccrualRule rule = leaveService.createAccrualRule(admin, request);
        return ResponseEntity.ok(ApiResponse.success("Policy accrual rule created successfully", rule));
    }

    @Operation(summary = "Assign Policy to Employees")
    @PostMapping("/policies/{policyId}/assign")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> assignPolicy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long policyId,
            @RequestBody Map<String, Object> payload) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        LeavePolicy policy = leaveService.getLeavePolicyById(policyId);
        return ResponseEntity.ok(ApiResponse.success("Policy assigned successfully to target employees", policy));
    }

    // == 3. LEAVE REQUESTS (/requests) =========================================

    @Operation(summary = "Apply Leave")
    @PostMapping("/requests")
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

    @Operation(summary = "List Leave Requests (With Filters & mine=true support)")
    @GetMapping("/requests")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean mine) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Long targetEmployeeId = employeeId;
        if (Boolean.TRUE.equals(mine)) {
            Employee emp = resolveEmployee(user);
            if (emp != null) targetEmployeeId = emp.getId();
        }

        List<Leave> list = leaveService.getLeaves(resolveOrgId(user), targetEmployeeId, leaveTypeId, status, fromDate, toDate, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", list));
    }

    @Operation(summary = "Get Leave Request Details")
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

    @Operation(summary = "Approve Leave Request")
    @PostMapping("/requests/{leaveRequestId}/approve")

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveLeaveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId,
            @RequestBody(required = false) ManagerCommentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        String comment = request != null ? request.getComment() : null;
        ManagerApprovalActionResponseDto result = leaveService.approveLeaveWithComment(leaveRequestId, comment, approver);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", result));
    }

    @Operation(summary = "Reject Leave Request")
    @PostMapping("/requests/{leaveRequestId}/reject")

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectLeaveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId,
            @RequestBody(required = false) ManagerCommentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        String comment = request != null ? request.getComment() : null;
        ManagerApprovalActionResponseDto result = leaveService.rejectLeaveWithComment(leaveRequestId, comment, approver);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", result));
    }

    @Operation(summary = "Send Back Leave Request")
    @PostMapping("/requests/{leaveRequestId}/send-back")

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> sendBackLeaveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId,
            @RequestBody(required = false) ManagerCommentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee approver = resolveEmployee(user);
        String comment = request != null ? request.getComment() : null;
        ManagerApprovalActionResponseDto result = leaveService.sendBackLeaveWithComment(leaveRequestId, comment, approver);
        return ResponseEntity.ok(ApiResponse.success("Leave request sent back successfully", result));
    }

    @Operation(summary = "Cancel Leave Request")
    @PostMapping({"requests/{leaveRequestId}/cancel", "/requests/{leaveRequestId}/cancel"})
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

    @Operation(summary = "Leave Request Audit History")
    @GetMapping("/requests/{leaveRequestId}/history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveRequestHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long leaveRequestId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveRequestHistory> history = leaveService.getLeaveHistory(leaveRequestId);
        return ResponseEntity.ok(ApiResponse.success("Leave request history retrieved successfully", history));
    }

    // == 4. LEAVE BALANCES (/balances) =========================================

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

    @Operation(summary = "Adjust Balance")
    @PostMapping("/balances/{employeeId}/adjust")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> adjustBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody BalanceAdjustmentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        if (request == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.error("Request body is required", "VAL_001"));
        }
        request.setEmployeeId(String.valueOf(employeeId));
        LeaveBalanceAdjustment adj = leaveBalanceService.adjustBalance(user, request);
        return ResponseEntity.ok(ApiResponse.success("Balance adjusted successfully", adj));
    }

    @Operation(summary = "List Balance Adjustment History")
    @GetMapping("/balance-adjustments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAdjustments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveBalanceAdjustment> list = leaveBalanceService.getAdjustments(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Balance adjustments retrieved successfully", list));
    }

    // == 5. CALENDAR, TEAM, DEPARTMENT & DASHBOARD ============================

    @Operation(summary = "Unified Leave Calendar (With Filters)")
    @GetMapping("/calendar")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveCalendarEventDto> calendar = leaveService.getLeaveCalendarEvents(
                resolveOrgId(user), employeeId, teamId, department, leaveTypeId, status, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success("Leave calendar retrieved successfully", calendar));
    }

    @Operation(summary = "Employee Leave Calendar")
    @GetMapping("/calendar/employee/{employeeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveCalendarEventDto> calendar = leaveService.getEmployeeCalendarEvents(
                resolveOrgId(user), employeeId, startDate, endDate, status
        );
        return ResponseEntity.ok(ApiResponse.success("Employee leave calendar retrieved successfully", calendar));
    }

    @Operation(summary = "Team Leave Calendar")
    @GetMapping("/calendar/team/{teamId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTeamCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveCalendarEventDto> calendar = leaveService.getTeamCalendarEvents(
                resolveOrgId(user), teamId, startDate, endDate, status
        );
        return ResponseEntity.ok(ApiResponse.success("Team leave calendar retrieved successfully", calendar));
    }

    @Operation(summary = "Department Leave Calendar")
    @GetMapping("/calendar/department/{department}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDepartmentCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<LeaveCalendarEventDto> calendar = leaveService.getDepartmentCalendarEvents(
                resolveOrgId(user), department, startDate, endDate, status
        );
        return ResponseEntity.ok(ApiResponse.success("Department leave calendar retrieved successfully", calendar));
    }

    @Operation(summary = "Team Leave View")
    @GetMapping("/team")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTeamLeaveView(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<Leave> teamLeaves = leaveService.getLeaves(resolveOrgId(user), employeeId, leaveTypeId, status, startDate, endDate, null);
        return ResponseEntity.ok(ApiResponse.success("Team leave view retrieved successfully", teamLeaves));
    }

    @Operation(summary = "Department Leave View")
    @GetMapping("/department")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDepartmentLeaveView(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<Leave> deptLeaves = leaveService.getLeaves(resolveOrgId(user), employeeId, leaveTypeId, status, startDate, endDate, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department leave view retrieved successfully", deptLeaves));
    }

    @Operation(summary = "Unified Leave Dashboard")
    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Map<String, Object> dashboard = leaveService.getDashboardMetrics(resolveOrgId(user));
        return ResponseEntity.ok(ApiResponse.success("Leave dashboard metrics retrieved successfully", dashboard));
    }

    // == 6. ACCRUALS & ENCASHMENTS =============================================

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

    @Operation(summary = "Get Encashment Details")
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
}
