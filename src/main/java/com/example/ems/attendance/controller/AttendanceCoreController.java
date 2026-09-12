package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.attendance.dto.AttendanceDaySummaryDto;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance")
@CrossOrigin("*")
@Tag(name = "Attendance Management Core", description = "Canonical Employee Attendance Lifecycle APIs: Check-In, Take Break, Resume Break, Check-Out, and Day Summary")
public class AttendanceCoreController {

    @Autowired
    private AttendanceService attendanceService;

    @Operation(summary = "Employee Check-In", description = "Records daily check-in for the authenticated employee in the active tenant organization. Transitions state from NOT_CHECKED_IN to WORKING.")
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceCoreResponse>> checkIn() {
        AttendanceCoreResponse response = attendanceService.checkInCore();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Checked in successfully", response));
    }

    @Operation(summary = "Start Break", description = "Starts an attendance break for the authenticated employee. State transitions from WORKING to ON_BREAK.")
    @PostMapping("/break/start")
    public ResponseEntity<ApiResponse<AttendanceCoreResponse>> startBreak() {
        AttendanceCoreResponse response = attendanceService.startBreakCore();
        return ResponseEntity.ok(ApiResponse.success("Break started successfully", response));
    }

    @Operation(summary = "End Break", description = "Ends the active attendance break and calculates break duration. State transitions from ON_BREAK back to WORKING.")
    @PostMapping("/break/end")
    public ResponseEntity<ApiResponse<AttendanceCoreResponse>> endBreak() {
        AttendanceCoreResponse response = attendanceService.endBreakCore();
        return ResponseEntity.ok(ApiResponse.success("Break ended successfully. Resumed work.", response));
    }

    @Operation(summary = "Employee Check-Out", description = "Completes daily attendance, calculates total break time, total attendance time, and net working duration. Transitions state to COMPLETED.")
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceCoreResponse>> checkOut() {
        AttendanceCoreResponse response = attendanceService.checkOutCore();
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully. Attendance completed.", response));
    }

    @Operation(summary = "Get Today's Attendance", description = "Retrieves today's attendance status and break intervals for the authenticated employee. Returns NOT_CHECKED_IN if no punch exists.")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceCoreResponse>> getTodayAttendance() {
        AttendanceCoreResponse response = attendanceService.getTodayAttendanceCore();
        return ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved successfully", response));
    }

    @Operation(summary = "Get Attendance Day Summary", description = "Retrieves complete daily attendance calculation breakdown (audit timestamps, grace, permission, payable minutes, and overtime) for an employee on a given date.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AttendanceDaySummaryDto>> getAttendanceDaySummary(
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        AttendanceDaySummaryDto response = attendanceService.getAttendanceDaySummary(employeeId, effectiveDate);
        return ResponseEntity.ok(ApiResponse.success("Attendance day summary retrieved successfully", response));
    }

    @Operation(summary = "Get Attendance By ID", description = "Retrieves an attendance record by ID, scoped to both current organization and authenticated employee.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceCoreResponse>> getAttendanceById(@PathVariable("id") Long id) {
        AttendanceCoreResponse response = attendanceService.getAttendanceByIdCore(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance details retrieved successfully", response));
    }

    @Operation(summary = "Get Attendance History", description = "Retrieves paginated attendance history for the authenticated employee with optional date range and status filters.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.example.ems.attendance.dto.AttendanceHistoryItemDto>>> getAttendanceHistory(
            @ModelAttribute com.example.ems.attendance.dto.AttendanceHistoryQuery query) {
        org.springframework.data.domain.Page<com.example.ems.attendance.dto.AttendanceHistoryItemDto> response = attendanceService.getAttendanceHistory(query);
        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved successfully", response));
    }
}
