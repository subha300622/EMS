package com.example.ems.leave.controller;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.*;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.entity.LeavePolicy;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.security.service.JwtService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Leave Management")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private RoleService roleService;

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

    private Employee resolveEmployee(User currentUser) {
        if (currentUser == null)
            return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    // ── 1. APPLY LEAVE ────────────────────────────────────────────────────────
    @Operation(summary = "Apply Leave", description = "Submits a new leave request with date range, leave type, and reason.", tags = {
            "Leave Management" })
    @PostMapping("/leaves")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> applyLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid LeaveRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        try {
            Leave record = leaveService.applyLeave(employee, request);
            ApplyLeaveResponseDto responseDto = new ApplyLeaveResponseDto(
                    record.getId(),
                    record.getStatus(),
                    record.getApprover() != null ? record.getApprover().getId() : null,
                    record.getApprover() != null ? record.getApprover().getFullName() : null,
                    "Leave request submitted successfully");
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Leave request submitted successfully", responseDto));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_001"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_001"));
        }
    }

    // ── 2. GET LEAVES (ALL OR MY) ─────────────────────────────────────────────
    @Operation(summary = "Get Leaves", description = "Retrieves leave request applications. If my=true or employeeId=me, retrieves the logged-in employee's leave applications. Otherwise, Admin/HR API to retrieve all leave requests.", tags = {
            "Leave Management" })
    @GetMapping("/leaves")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Leave>>> getLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Boolean my,
            @RequestParam(required = false) String employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (Boolean.TRUE.equals(my) || "me".equalsIgnoreCase(employeeId)) {
            Employee employee = resolveEmployee(currentUser);
            if (employee == null) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
            }
            return ResponseEntity.ok(ApiResponse.success("Leave history retrieved successfully",
                    leaveService.getLeavesByEmployeeId(employee.getId())));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "leave.approve")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(
                            "Access Denied: Requires 'leave.read', 'leave.approve', or 'leave.manage' permission.",
                            "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave records retrieved successfully",
                leaveService.getAllLeaves()));
    }

    // ── 2b. GET MY LEAVE REQUESTS ─────────────────────────────────────────────
    @Operation(summary = "My Leave Requests", description = "Retrieves leave requests submitted by the logged-in employee.", tags = {
            "Leave Management" })
    @GetMapping("/leaves/my-requests")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Leave>>> getMyLeaveRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("My leave requests retrieved successfully",
                leaveService.getLeavesByEmployeeId(employee.getId())));
    }

    // ── 3. GET LEAVE BY ID ────────────────────────────────────────────────────
    @Operation(summary = "Leave Details", description = "Retrieves details of a specific leave application by its ID.", tags = {
            "Leave Management" })
    @GetMapping("/leaves/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getLeaveById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Leave leave = leaveService.getLeaveById(id).orElse(null);
        if (leave == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Leave request not found with ID: " + id, "LV_002"));
        }

        Employee employee = resolveEmployee(currentUser);
        boolean isOwner = employee != null && leave.getEmployee().getId().equals(employee.getId());
        boolean hasAccess = isOwner
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.approve");

        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this leave request.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave request retrieved successfully", leave));
    }

    // ── 8. GET PENDING LEAVES (MANAGERS / ADMIN / HR) ──────────────────────────
    @Operation(summary = "Get Pending Leaves", description = "Retrieves all leave applications currently awaiting approval decisions.")
    @GetMapping("/leaves/pending")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Leave>>> getPendingLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasAccess = roleService.hasPermission(currentUser.getWorkEmail(), "leave.approve")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.team.approve")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage");

        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires approval permissions.", "AUTH_002"));
        }

        // Return all pending leaves. (Managers can filter in frontend or we can return
        // all)
        return ResponseEntity.ok(ApiResponse.success("Pending leaves retrieved successfully",
                leaveService.getPendingLeaves()));
    }

    // ── 9. GET STATS (ADMIN / HR) ─────────────────────────────────────────────
    @Operation(summary = "Get Leave Statistics", description = "Retrieves statistics on leave applications and balances.")
    @GetMapping("/leaves/stats")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeaveStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires read permissions.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave statistics retrieved successfully",
                leaveService.getLeaveStats()));
    }

    // ── 10. APPROVE LEAVE ─────────────────────────────────────────────────────
    @Operation(summary = "Approve Leave Request", description = "Approves a pending leave application.")
    @PatchMapping("/leaves/{id}/approve")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> approveLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(value = "id") Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasAccess = roleService.hasPermission(currentUser.getWorkEmail(), "leave.approve")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.team.approve")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage");

        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires approval permissions.", "AUTH_002"));
        }

        Employee approver = resolveEmployee(currentUser);
        if (approver == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Approver employee profile not found", "EMP_002"));
        }

        try {
            Leave record = leaveService.approveLeave(id, approver);
            return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_003"));
        }
    }

    // ── 11. REJECT LEAVE ─────────────────────────────────────────────────────
    @Operation(summary = "Reject Leave Request", description = "Rejects a pending leave application with feedback options.")
    @PatchMapping("/leaves/{id}/reject")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> rejectLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(value = "id") Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasAccess = roleService.hasPermission(currentUser.getWorkEmail(), "leave.approve")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.team.approve")
                || roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage");

        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires approval permissions.", "AUTH_002"));
        }

        Employee approver = resolveEmployee(currentUser);
        if (approver == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Approver employee profile not found", "EMP_002"));
        }

        try {
            Leave record = leaveService.rejectLeave(id, approver);
            return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_004"));
        }
    }

    // ── 11b. MANAGER LEAVE APPROVALS ──────────────────────────────────────────
    @Operation(summary = "Manager Leave Approvals", description = "Retrieves leave requests assigned to the logged-in manager with pagination and filters.", tags = {
            "Leave Approvals" })
    @GetMapping("/manager/leave-approvals")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Page<LeaveApprovalResponseDto>>> getManagerLeaveApprovals(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "status", required = false, defaultValue = "PENDING") String status,
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestParam(name = "fromDate", required = false) String fromDateStr,
            @RequestParam(name = "toDate", required = false) String toDateStr) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_002"));
        }

        LocalDate fromDate = fromDateStr != null && !fromDateStr.isBlank() ? LocalDate.parse(fromDateStr) : null;
        LocalDate toDate = toDateStr != null && !toDateStr.isBlank() ? LocalDate.parse(toDateStr) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<LeaveApprovalResponseDto> result = leaveService.getManagerLeaveApprovals(
                manager, status, employeeId, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Manager leave approvals retrieved successfully", result));
    }

    @Operation(summary = "Approval Details", description = "Retrieves detailed information for a leave request assigned to the logged-in manager.", tags = {
            "Leave Approvals" })
    @GetMapping("/manager/leave-approvals/{leaveId}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<LeaveApprovalResponseDto>> getManagerLeaveApprovalDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(name = "leaveId") Long leaveId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_002"));
        }

        try {
            LeaveApprovalResponseDto details = leaveService.getManagerLeaveApprovalDetails(leaveId, manager);
            return ResponseEntity.ok(ApiResponse.success("Leave approval details retrieved successfully", details));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "LV_002"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Approval Summary", description = "Retrieves dashboard summary statistics for the logged-in manager's assigned leave requests.", tags = {
            "Leave Approvals" })
    @GetMapping("/manager/leave-approvals/summary")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<LeaveApprovalSummaryDto>> getLeaveApprovalSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_002"));
        }

        LeaveApprovalSummaryDto summary = leaveService.getLeaveApprovalSummary(manager);
        return ResponseEntity.ok(ApiResponse.success("Leave approval summary retrieved successfully", summary));
    }

    @Operation(summary = "Approve Leave", description = "Approves a pending leave request with a manager comment.", tags = {
            "Leave Approvals" })
    @PostMapping("/manager/leave-approvals/{leaveId}/approve")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<ManagerApprovalActionResponseDto>> approveLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(name = "leaveId") Long leaveId,
            @RequestBody(required = false) @Valid ManagerCommentRequest commentRequest) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_002"));
        }

        String comment = commentRequest != null ? commentRequest.getComment() : null;

        try {
            ManagerApprovalActionResponseDto result = leaveService.approveLeaveWithComment(leaveId, comment, manager);
            return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", result));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_003"));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_003"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Reject Leave", description = "Rejects a pending leave request with a manager comment.", tags = {
            "Leave Approvals" })
    @PostMapping("/manager/leave-approvals/{leaveId}/reject")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<ManagerApprovalActionResponseDto>> rejectLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(name = "leaveId") Long leaveId,
            @RequestBody(required = false) @Valid ManagerCommentRequest commentRequest) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_002"));
        }

        String comment = commentRequest != null ? commentRequest.getComment() : null;

        try {
            ManagerApprovalActionResponseDto result = leaveService.rejectLeaveWithComment(leaveId, comment, manager);
            return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", result));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_004"));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_004"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Bulk Approve", description = "Approves multiple pending leave requests in bulk with a manager comment.", tags = {
            "Leave Approvals" })
    @PostMapping("/manager/leave-approvals/bulk-approve")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> bulkApprove(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid BulkApprovalRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_002"));
        }

        try {
            leaveService.bulkApproveLeaves(request.getLeaveIds(), request.getComment(), manager);
            return ResponseEntity.ok(ApiResponse.success("Bulk approval successful"));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_003"));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_003"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Bulk Reject", description = "Rejects multiple pending leave requests in bulk with a manager comment.", tags = {
            "Leave Approvals" })
    @PostMapping("/manager/leave-approvals/bulk-reject")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> bulkReject(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid BulkApprovalRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_002"));
        }

        try {
            leaveService.bulkRejectLeaves(request.getLeaveIds(), request.getComment(), manager);
            return ResponseEntity.ok(ApiResponse.success("Bulk rejection successful"));
        } catch (BadRequestException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_004"));
        } catch (AccessDeniedException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_004"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    // ── 12. CANCEL LEAVE ─────────────────────────────────────────────────────
    @Operation(summary = "Cancel Leave", description = "Cancels a submitted leave application.", tags = {
            "Leave Management" })
    @PutMapping("/leaves/cancel")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> cancelLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "leaveId") Long leaveId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            Leave record = leaveService.cancelLeave(leaveId, employee);
            return ResponseEntity.ok(ApiResponse.success("Leave request cancelled successfully", record));
        } catch (IllegalArgumentException | SecurityException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_005"));
        }
    }

    // ── 12b. DELETE LEAVE ─────────────────────────────────────────────────────
    @Operation(summary = "Delete Leave Entry", description = "Deletes a leave application entry from records.")
    @DeleteMapping("/leaves")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> deleteLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "leaveId") Long leaveId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            leaveService.deleteLeave(leaveId);
            return ResponseEntity.ok(ApiResponse.success("Leave request deleted successfully"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "LV_002"));
        }
    }

    // ── 13. CREATE LEAVE TYPE ─────────────────────────────────────────────────
    @Operation(summary = "Create Leave Type", description = "Creates a new category class for leave allocation, like Paid Leave or Sick Leave.")
    @PostMapping("/leave-types")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> createLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid LeaveTypeRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            LeaveType type = leaveService.createLeaveType(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Leave type created successfully", type));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LVT_001"));
        }
    }

    // ── 14. GET ALL LEAVE TYPES ───────────────────────────────────────────────
    @Operation(summary = "Get All Leave Types", description = "Retrieves a listing of all active and inactive leave type classifications.")
    @GetMapping("/leave-types")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<LeaveType>>> getLeaveTypes(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave types retrieved successfully",
                leaveService.getAllLeaveTypes()));
    }

    // ── 15. UPDATE LEAVE TYPE ────────────────────────────────────────────────
    @Operation(summary = "Update Leave Type", description = "Updates configurations on an existing leave type.")
    @PutMapping("/leave-types/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid LeaveTypeRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            LeaveType type = leaveService.updateLeaveType(id, request);
            return ResponseEntity.ok(ApiResponse.success("Leave type updated successfully", type));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LVT_002"));
        }
    }

    // ── 16. DEACTIVATE LEAVE TYPE ─────────────────────────────────────────────
    @Operation(summary = "Deactivate Leave Type", description = "Deactivates a leave type classification, disabling new applications.")
    @PatchMapping("/leave-types/{id}/deactivate")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> deactivateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            LeaveType type = leaveService.deactivateLeaveType(id);
            return ResponseEntity.ok(ApiResponse.success("Leave type deactivated successfully", type));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LVT_003"));
        }
    }

    // ── 16b. ACTIVATE LEAVE TYPE ─────────────────────────────────────────────
    @Operation(summary = "Activate Leave Type", description = "Activates a previously deactivated leave type classification.")
    @PatchMapping("/leave-types/{id}/activate")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> activateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            LeaveType type = leaveService.activateLeaveType(id);
            return ResponseEntity.ok(ApiResponse.success("Leave type activated successfully", type));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LVT_003"));
        }
    }

    // ── 17. GET LEAVE TYPE BY ID ──────────────────────────────────────────────
    @Operation(summary = "Get Leave Type Details", description = "Retrieves details of a specific leave type by ID.")
    @GetMapping("/leave-types/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getLeaveTypeById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Optional<LeaveType> typeOpt = leaveService.getLeaveTypeById(id);
        if (typeOpt.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Leave type not found with ID: " + id, "LVT_004"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave type retrieved successfully", typeOpt.get()));
    }

    // ── 18. DELETE LEAVE TYPE ──────────────────────────────────────────────────
    @Operation(summary = "Delete Leave Type", description = "Removes a leave type classification from the system configuration.")
    @DeleteMapping("/leave-types/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> deleteLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            leaveService.deleteLeaveType(id);
            return ResponseEntity.ok(ApiResponse.success("Leave type deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "LVT_005"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Cannot delete leave type: " + e.getMessage(), "LVT_006"));
        }
    }

    // ── 19. GET LEAVE CALENDAR ───────────────────────────────────────────────
    @Operation(summary = "Get Leave Calendar", description = "Retrieves a calendar timeline of active leaves.")
    @GetMapping("/leave-calendar")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<Leave>>> getLeaveCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        // Leave calendar is available to all authenticated users, showing active leaves
        return ResponseEntity.ok(ApiResponse.success("Leave calendar retrieved successfully",
                leaveService.getAllLeaves()));
    }

    // ── 20. GET LEAVES PAYROLL IMPACT ─────────────────────────────────────────
    @Operation(summary = "Get Leaves Payroll Impact", description = "Retrieves unpaid leave details for payroll deductions.")
    @GetMapping("/leaves/payroll-impact")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeavesPayrollImpact(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String month) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> impact = new java.util.LinkedHashMap<>();
        impact.put("unpaidLeave", 2);

        return ResponseEntity.ok(ApiResponse.success("Leaves payroll impact retrieved successfully", impact));
    }

    // ── Leave Balance and Policy Mappings ───────────────────────────────────
    @Operation(summary = "Get My Leave Balance", description = "Retrieves leave balances for the currently logged in employee.")
    @GetMapping("/leaves/balance")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getMyLeaveBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave balance retrieved successfully",
                leaveService.getLeaveBalance(employee.getId())));
    }

    @Operation(summary = "Get Employee Leave Balance", description = "Admin/HR API to retrieve leave balances for a specific employee.")
    @GetMapping("/leaves/balance/{employeeId}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getEmployeeLeaveBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.read' or 'leave.manage' permission.",
                            "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave balance retrieved successfully",
                leaveService.getLeaveBalance(employeeId)));
    }

    @Operation(summary = "Get All Leave Policies", description = "Retrieves configurations for all leave policies.")
    @GetMapping("/leave-policies")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<LeavePolicy>>> getLeavePolicies(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave policies retrieved successfully",
                leaveService.getAllLeavePolicies()));
    }

    @Operation(summary = "Create Leave Policy", description = "Creates a new leave policy rule configuration.")
    @PostMapping("/leave-policies")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> createLeavePolicy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid LeavePolicyRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            LeavePolicy policy = leaveService.createLeavePolicy(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Leave policy created successfully", policy));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LVP_001"));
        }
    }

    @Operation(summary = "Update Leave Policy", description = "Updates settings on a specific leave policy rule.")
    @PutMapping("/leave-policies/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateLeavePolicy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid LeavePolicyRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "leave.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'leave.manage' permission.", "AUTH_002"));
        }

        try {
            LeavePolicy policy = leaveService.updateLeavePolicy(id, request);
            return ResponseEntity.ok(ApiResponse.success("Leave policy updated successfully", policy));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LVP_002"));
        }
    }
}
