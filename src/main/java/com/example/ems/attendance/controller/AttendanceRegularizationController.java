package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.CreateRegularizationRequest;
import com.example.ems.attendance.dto.RegularizationApprovalRequest;
import com.example.ems.attendance.dto.RegularizationResponseDto;
import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
import com.example.ems.attendance.service.AttendanceRegularizationService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance/regularizations")
@CrossOrigin("*")
@Tag(name = "Attendance Regularization", description = "Self-Service Attendance Correction Requests and Central Approval Management")
public class AttendanceRegularizationController {

    @Autowired
    private AttendanceRegularizationService regularizationService;

    @Operation(summary = "Create Regularization Request", description = "Submits an attendance correction request for the authenticated employee. Starts a central approval workflow instance.")
    @PostMapping
    public ResponseEntity<ApiResponse<RegularizationResponseDto>> createRegularization(
            @Valid @RequestBody CreateRegularizationRequest request) {
        RegularizationResponseDto response = regularizationService.createRegularization(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance regularization request submitted successfully", response));
    }

    @Operation(summary = "Get My Regularization Requests", description = "Retrieves paginated regularization requests for the authenticated employee in the current organization.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RegularizationResponseDto>>> getMyRegularizations(
            @RequestParam(name = "status", required = false) AttendanceRegularizationStatus status,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Page<RegularizationResponseDto> response = regularizationService.getMyRegularizations(status, fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.success("Regularization requests retrieved successfully", response));
    }

    @Operation(summary = "Get Regularization By ID", description = "Retrieves a single regularization request by ID, strictly scoped to the authenticated employee and tenant.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegularizationResponseDto>> getRegularizationById(@PathVariable("id") Long id) {
        RegularizationResponseDto response = regularizationService.getRegularizationById(id);
        return ResponseEntity.ok(ApiResponse.success("Regularization request details retrieved successfully", response));
    }

    @Operation(summary = "Cancel Regularization Request", description = "Cancels a PENDING regularization request. Can only be cancelled by the owner employee before approval.")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RegularizationResponseDto>> cancelRegularization(
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", required = false) String reason) {
        RegularizationResponseDto response = regularizationService.cancelRegularization(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Regularization request cancelled successfully", response));
    }

    @Operation(summary = "Approve Regularization", description = "Approves a pending regularization request and triggers attendance time recalculation.")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RegularizationResponseDto>> approveRegularization(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) RegularizationApprovalRequest request) {
        RegularizationResponseDto response = regularizationService.approveRegularization(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance regularization approved successfully", response));
    }

    @Operation(summary = "Reject Regularization", description = "Rejects a pending regularization request without modifying attendance timestamps.")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RegularizationResponseDto>> rejectRegularization(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) RegularizationApprovalRequest request) {
        RegularizationResponseDto response = regularizationService.rejectRegularization(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance regularization rejected successfully", response));
    }
}
