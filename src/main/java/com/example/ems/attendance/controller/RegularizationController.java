package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.AttendanceRegularizationRequest;
import com.example.ems.attendance.dto.RegularizationProcessRequest;
import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.service.AttendanceRegularizationService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Attendance Regularization Management")
public class RegularizationController {

    @Autowired
    private AttendanceRegularizationService regularizationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    private Employee resolveEmployee(User currentUser) {
        if (currentUser == null) return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    @Operation(summary = "Submit Attendance Regularization", description = "Submits a request to correct attendance check-in/out times.")
    @PostMapping("/attendance/regularization")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> submitRegularization(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid AttendanceRegularizationRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long empId = request.getEmployeeId();
        if (empId == null) {
            Employee employee = resolveEmployee(currentUser);
            if (employee == null) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
            }
            empId = employee.getId();
        }

        try {
            AttendanceRegularization record = regularizationService.submitRegularization(
                    empId, request.getDate(), request.getProposedPunchInTime(), request.getProposedPunchOutTime(), request.getReason());
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Regularization request submitted successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_005"));
        }
    }

    @Operation(summary = "Get Attendance Regularizations", description = "Retrieves a list of all attendance regularization requests.")
    @GetMapping("/attendance/regularization")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<AttendanceRegularization>>> getRegularizations(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            List<AttendanceRegularization> list = regularizationService.getRegularizationsForUser(currentUser, status);
            return ResponseEntity.ok(ApiResponse.success("Regularization requests retrieved successfully", list));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Approve Attendance Regularization", description = "Approves a pending regularization request and updates the attendance record.")
    @PatchMapping("/attendance/regularization/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveRegularization(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody(required = false) RegularizationProcessRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.manage' permission.", "AUTH_002"));
        }

        try {
            AttendanceRegularization record = regularizationService.approveRegularization(id, request);
            return ResponseEntity.ok(ApiResponse.success("Regularization request approved successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_006"));
        }
    }

    @Operation(summary = "Reject Attendance Regularization", description = "Rejects a pending regularization request.")
    @PatchMapping("/attendance/regularization/{id}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectRegularization(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody(required = false) RegularizationProcessRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.manage' permission.", "AUTH_002"));
        }

        try {
            AttendanceRegularization record = regularizationService.rejectRegularization(id, request);
            return ResponseEntity.ok(ApiResponse.success("Regularization request rejected successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_007"));
        }
    }
}
