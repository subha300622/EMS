package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.dto.adjustment.AdjustmentResponseDto;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.entity.AttendanceAdjustmentStatus;
import com.example.ems.attendance.service.AttendanceAdjustmentService;
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
@RequestMapping("/api/v1/attendance")
@CrossOrigin("*")
@Tag(name = "Attendance Adjustment", description = "Attendance Record Adjustments and Approval Management APIs")
public class AttendanceAdjustmentController {

    @Autowired
    private AttendanceAdjustmentService adjustmentService;

    @Operation(summary = "Create Attendance Adjustment Request", description = "Submits an adjustment request for a specific attendance record. Initiates approval workflow.")
    @PostMapping("/{attendanceId}/adjustments")
    public ResponseEntity<ApiResponse<AdjustmentResponseDto>> createAdjustment(
            @PathVariable("attendanceId") Long attendanceId,
            @Valid @RequestBody CreateAdjustmentRequest request) {
        AdjustmentResponseDto response = adjustmentService.createAdjustment(attendanceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance adjustment request submitted successfully", response));
    }

    @Operation(summary = "Get Attendance Adjustments", description = "Retrieves paginated attendance adjustment requests with optional filtering by employee, status, and date range.")
    @GetMapping("/adjustments")
    public ResponseEntity<ApiResponse<Page<AdjustmentResponseDto>>> getAdjustments(
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestParam(name = "status", required = false) AttendanceAdjustmentStatus status,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Page<AdjustmentResponseDto> response = adjustmentService.getAdjustments(employeeId, status, fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.success("Attendance adjustments retrieved successfully", response));
    }

    @Operation(summary = "Get Adjustment By ID", description = "Retrieves a single attendance adjustment request by ID.")
    @GetMapping("/adjustments/{id}")
    public ResponseEntity<ApiResponse<AdjustmentResponseDto>> getAdjustmentById(@PathVariable("id") Long id) {
        AdjustmentResponseDto response = adjustmentService.getAdjustmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance adjustment retrieved successfully", response));
    }

    @Operation(summary = "Cancel Adjustment Request", description = "Cancels a pending attendance adjustment request.")
    @PostMapping("/adjustments/{id}/cancel")
    public ResponseEntity<ApiResponse<AdjustmentResponseDto>> cancelAdjustment(
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", required = false) String reason) {
        AdjustmentResponseDto response = adjustmentService.cancelAdjustment(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Attendance adjustment cancelled successfully", response));
    }

    @Operation(summary = "Approve Adjustment Request", description = "Approves a pending attendance adjustment and applies corrections to the attendance record.")
    @PostMapping("/adjustments/{id}/approve")
    public ResponseEntity<ApiResponse<AdjustmentResponseDto>> approveAdjustment(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) AdjustmentApprovalRequest request) {
        AdjustmentResponseDto response = adjustmentService.approveAdjustment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance adjustment approved successfully", response));
    }

    @Operation(summary = "Reject Adjustment Request", description = "Rejects a pending attendance adjustment request.")
    @PostMapping("/adjustments/{id}/reject")
    public ResponseEntity<ApiResponse<AdjustmentResponseDto>> rejectAdjustment(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) AdjustmentApprovalRequest request) {
        AdjustmentResponseDto response = adjustmentService.rejectAdjustment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance adjustment rejected successfully", response));
    }
}
