package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.AttendanceCalendarDayDto;
import com.example.ems.attendance.dto.MonthlyAttendanceCalendarResponse;
import com.example.ems.attendance.service.AttendanceCalendarService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance/calendar")
@CrossOrigin("*")
@Tag(name = "Attendance Calendar", description = "Self-Service Monthly and Daily Attendance Calendar APIs")
public class AttendanceCalendarController {

    @Autowired
    private AttendanceCalendarService attendanceCalendarService;

    @Operation(summary = "Get Monthly Attendance Calendar", description = "Returns the authenticated employee's attendance calendar for the specified year and month, including present, absent, half day, leave, holiday, and week off days.")
    @GetMapping
    public ResponseEntity<ApiResponse<MonthlyAttendanceCalendarResponse>> getMonthlyCalendar(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month) {

        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.success("Monthly attendance calendar retrieved successfully", response));
    }

    @Operation(summary = "Get Attendance Calendar Day Detail", description = "Returns detailed attendance information for one calendar date for the authenticated employee.")
    @GetMapping("/{date}")
    public ResponseEntity<ApiResponse<AttendanceCalendarDayDto>> getDayDetail(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AttendanceCalendarDayDto response = attendanceCalendarService.getDayDetail(date);
        return ResponseEntity.ok(ApiResponse.success("Attendance day detail retrieved successfully", response));
    }
}
