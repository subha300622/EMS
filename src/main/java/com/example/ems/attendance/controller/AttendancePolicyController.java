package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.policy.AttendancePolicyDto;
import com.example.ems.attendance.dto.policy.CreateAttendancePolicyRequest;
import com.example.ems.attendance.dto.policy.UpdateAttendancePolicyRequest;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.service.AttendancePolicyService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance/policies")
@CrossOrigin("*")
@Tag(name = "Attendance Policy Management", description = "Tenant Attendance Configuration, Start/End times, Grace Periods, and Thresholds")
public class AttendancePolicyController {

    @Autowired
    private AttendancePolicyService policyService;

    @Operation(summary = "Get All Attendance Policies", description = "Retrieves paginated attendance policies for the authenticated organization.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AttendancePolicyDto>>> getAllPolicies(
            @RequestParam(name = "status", required = false) AttendancePolicyStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Page<AttendancePolicyDto> response = policyService.getAllPolicies(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Attendance policies retrieved successfully", response));
    }

    @Operation(summary = "Create Attendance Policy", description = "Creates a new attendance policy in DRAFT status for the authenticated organization.")
    @PostMapping
    public ResponseEntity<ApiResponse<AttendancePolicyDto>> createPolicy(
            @Valid @RequestBody CreateAttendancePolicyRequest request) {
        AttendancePolicyDto response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance policy created successfully", response));
    }

    @Operation(summary = "Get Attendance Policy by ID", description = "Retrieves a single attendance policy by ID, scoped to the current tenant organization.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendancePolicyDto>> getPolicyById(@PathVariable("id") Long id) {
        AttendancePolicyDto response = policyService.getPolicyById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance policy retrieved successfully", response));
    }

    @Operation(summary = "Update Attendance Policy", description = "Updates an existing attendance policy within the current tenant organization.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendancePolicyDto>> updatePolicy(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateAttendancePolicyRequest request) {
        AttendancePolicyDto response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance policy updated successfully", response));
    }

    @Operation(summary = "Activate Attendance Policy", description = "Sets the policy status to ACTIVE and deactivates any other active policy for the tenant.")
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AttendancePolicyDto>> activatePolicy(@PathVariable("id") Long id) {
        AttendancePolicyDto response = policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance policy activated successfully", response));
    }

    @Operation(summary = "Deactivate Attendance Policy", description = "Sets the policy status to INACTIVE.")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<AttendancePolicyDto>> deactivatePolicy(@PathVariable("id") Long id) {
        AttendancePolicyDto response = policyService.deactivatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance policy deactivated successfully", response));
    }
}
