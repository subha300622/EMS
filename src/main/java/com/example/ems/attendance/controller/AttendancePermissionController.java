package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.permission.AttendancePermissionCreateRequest;
import com.example.ems.attendance.dto.permission.AttendancePermissionRejectRequest;
import com.example.ems.attendance.dto.permission.AttendancePermissionResponse;
import com.example.ems.attendance.entity.AttendancePermissionStatus;
import com.example.ems.attendance.service.AttendancePermissionService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance/permissions")
@CrossOrigin("*")
@Tag(name = "Attendance Permission Management", description = "Endpoints for employee attendance permission/exception requests, approvals, rejections, and quota controls.")
public class AttendancePermissionController {

    @Autowired
    private AttendancePermissionService permissionService;

    @Operation(summary = "Submit Attendance Permission Request", description = "Submits an attendance permission/adjustment request (e.g. LATE_ARRIVAL, EARLY_EXIT, MISSING_PUNCH).")
    @PostMapping
    @PreAuthorize("hasAuthority('attendance.permission.create') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendancePermissionResponse>> createPermission(
            @Valid @RequestBody AttendancePermissionCreateRequest request) {
        AttendancePermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance permission request submitted successfully", response));
    }

    @Operation(summary = "Get My Attendance Permissions", description = "Retrieves paginated attendance permissions for the currently authenticated employee.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('attendance.permission.read') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AttendancePermissionResponse>>> getMyPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "attendanceDate"));
        Page<AttendancePermissionResponse> response = permissionService.getMyPermissions(pageable);
        return ResponseEntity.ok(ApiResponse.success("Employee permissions retrieved successfully", response));
    }

    @Operation(summary = "Get Pending Permissions", description = "Retrieves all pending permission requests for management/HR review.")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('attendance.permission.approve') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AttendancePermissionResponse>>> getPendingPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AttendancePermissionResponse> response = permissionService.getPendingPermissions(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending attendance permissions retrieved successfully", response));
    }

    @Operation(summary = "Get Permission By ID", description = "Retrieves a single attendance permission details by its ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('attendance.permission.read') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendancePermissionResponse>> getPermissionById(@PathVariable("id") Long id) {
        AttendancePermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance permission details retrieved successfully", response));
    }

    @Operation(summary = "Approve Attendance Permission", description = "Approves a pending attendance permission and automatically applies adjustments to attendance while preserving audit timestamps.")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('attendance.permission.approve') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendancePermissionResponse>> approvePermission(
            @PathVariable("id") Long id,
            @RequestParam(value = "comments", required = false) String comments) {
        AttendancePermissionResponse response = permissionService.approvePermission(id, comments);
        return ResponseEntity.ok(ApiResponse.success("Attendance permission approved successfully", response));
    }

    @Operation(summary = "Reject Attendance Permission", description = "Rejects a pending attendance permission with mandatory reason.")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('attendance.permission.reject') or hasAuthority('attendance.permission.approve') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendancePermissionResponse>> rejectPermission(
            @PathVariable("id") Long id,
            @Valid @RequestBody AttendancePermissionRejectRequest request) {
        AttendancePermissionResponse response = permissionService.rejectPermission(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance permission rejected successfully", response));
    }

    @Operation(summary = "Cancel Attendance Permission", description = "Cancels a pending attendance permission request.")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('attendance.permission.create') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendancePermissionResponse>> cancelPermission(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        AttendancePermissionResponse response = permissionService.cancelPermission(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Attendance permission cancelled successfully", response));
    }

    @Operation(summary = "Search / Filter Attendance Permissions", description = "Queries permissions with multi-criteria filters for administration and reporting.")
    @GetMapping
    @PreAuthorize("hasAuthority('attendance.permission.read') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AttendancePermissionResponse>>> getPermissions(
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "status", required = false) AttendancePermissionStatus status,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AttendancePermissionResponse> response = permissionService.getPermissions(employeeId, status, fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.success("Permissions filtered successfully", response));
    }
}
