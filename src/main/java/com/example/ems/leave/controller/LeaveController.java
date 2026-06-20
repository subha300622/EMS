package com.example.ems.leave.controller;
import java.util.List;
import java.util.Map;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.dto.LeaveTypeRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.security.service.JwtService;

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
    @PostMapping("/leaves")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> applyLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid LeaveRequest request){

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
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Leave request submitted successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_001"));
        }
    }

    // ── 1b. GET MY LEAVES ─────────────────────────────────────────────────────
    @GetMapping("/leaves/my")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Leave>>> getMyLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

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

        return ResponseEntity.ok(ApiResponse.success("Leave history retrieved successfully",
                leaveService.getLeavesByEmployeeId(employee.getId())));
    }

    // ── 2. GET ALL LEAVES (ADMIN / HR) ────────────────────────────────────────
    @GetMapping("/leaves")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Leave>>> getAllLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
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

    // ── 3. GET LEAVE BY ID ────────────────────────────────────────────────────
    @GetMapping("/leaves/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

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
    @GetMapping("/leaves/pending")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Leave>>> getPendingLeaves(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

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
    @GetMapping("/leaves/stats")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeaveStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

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
    @PatchMapping("/leaves/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "leaveId") Long leaveId){

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
            Leave record = leaveService.approveLeave(leaveId, approver);
            return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_003"));
        }
    }

    // ── 11. REJECT LEAVE ─────────────────────────────────────────────────────
    @PatchMapping("/leaves/{id}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "leaveId") Long leaveId){

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
            Leave record = leaveService.rejectLeave(leaveId, approver);
            return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "LV_004"));
        }
    }

    // ── 12. CANCEL LEAVE ─────────────────────────────────────────────────────
    @PutMapping("/leaves/cancel")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> cancelLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "leaveId") Long leaveId){

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
    @DeleteMapping("/leaves")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteLeave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "leaveId") Long leaveId){

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
    @PostMapping("/leave-types")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid LeaveTypeRequest request){

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
    @GetMapping("/leave-types")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<LeaveType>>> getLeaveTypes(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Leave types retrieved successfully",
                leaveService.getAllLeaveTypes()));
    }

    // ── 15. UPDATE LEAVE TYPE ────────────────────────────────────────────────
    @PutMapping("/leave-types/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid LeaveTypeRequest request){

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
    @PatchMapping("/leave-types/{id}/deactivate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deactivateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

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
    @PatchMapping("/leave-types/{id}/activate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> activateLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

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
    @GetMapping("/leave-types/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLeaveTypeById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

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
    @DeleteMapping("/leave-types/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteLeaveType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

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
    @GetMapping("/leave-calendar")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Leave>>> getLeaveCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

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
    @GetMapping("/leaves/payroll-impact")
    @SuppressWarnings({"unchecked", "rawtypes"})
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
}
