package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.early.EarlyCheckoutQuery;
import com.example.ems.attendance.dto.early.EarlyCheckoutReportDto;
import com.example.ems.attendance.dto.late.LateAttendanceQuery;
import com.example.ems.attendance.dto.late.LateAttendanceReportDto;
import com.example.ems.attendance.service.AttendanceLateEarlyService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@CrossOrigin("*")
@Tag(name = "Late Attendance & Early Checkout", description = "Tenant Late Arrivals and Early Departure Reporting APIs")
public class AttendanceLateEarlyController {

    @Autowired
    private AttendanceLateEarlyService lateEarlyService;

    @Operation(summary = "Get Late Attendance Report", description = "Retrieves paginated late arrivals evaluated against the active attendance policy with date, department, team, and employee filters.")
    @GetMapping("/late")
    public ResponseEntity<ApiResponse<Page<LateAttendanceReportDto>>> getLateAttendanceReport(
            @ModelAttribute LateAttendanceQuery query) {
        Page<LateAttendanceReportDto> response = lateEarlyService.getLateAttendanceReport(query);
        return ResponseEntity.ok(ApiResponse.success("Late attendance report retrieved successfully", response));
    }

    @Operation(summary = "Get Early Checkout Report", description = "Retrieves paginated early checkouts evaluated against the active attendance policy with date, department, team, and employee filters.")
    @GetMapping("/early-checkout")
    public ResponseEntity<ApiResponse<Page<EarlyCheckoutReportDto>>> getEarlyCheckoutReport(
            @ModelAttribute EarlyCheckoutQuery query) {
        Page<EarlyCheckoutReportDto> response = lateEarlyService.getEarlyCheckoutReport(query);
        return ResponseEntity.ok(ApiResponse.success("Early checkout report retrieved successfully", response));
    }
}
