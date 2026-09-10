package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.*;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.service.DepartmentAttendanceService;
import com.example.ems.attendance.service.TeamAttendanceService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance")
@CrossOrigin("*")
@Tag(name = "Team & Department Attendance", description = "Team and Department Attendance Daily Summary and History APIs")
public class AttendanceTeamDepartmentController {

    @Autowired
    private TeamAttendanceService teamAttendanceService;

    @Autowired
    private DepartmentAttendanceService departmentAttendanceService;

    // ── Team Attendance Endpoints ───────────────────────────────────────────

    @Operation(summary = "Get Team Daily Attendance", description = "Returns team daily attendance rollup (total employees, present, absent, on leave, late) and member breakdown for a specific date.")
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<ApiResponse<TeamDailyAttendanceResponse>> getTeamDailyAttendance(
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "status", required = false) AttendanceStatus status) {

        TeamDailyAttendanceResponse response = teamAttendanceService.getTeamDailyAttendance(teamId, date, status);
        return ResponseEntity.ok(ApiResponse.success("Team daily attendance retrieved successfully", response));
    }

    @Operation(summary = "Get Team Attendance History", description = "Returns paginated attendance history for all active members of a team with optional date range and status filters.")
    @GetMapping("/teams/{teamId}/history")
    public ResponseEntity<ApiResponse<Page<TeamDepartmentAttendanceHistoryItemDto>>> getTeamAttendanceHistory(
            @PathVariable("teamId") Long teamId,
            @ModelAttribute TeamAttendanceHistoryQuery query) {

        Page<TeamDepartmentAttendanceHistoryItemDto> response = teamAttendanceService.getTeamAttendanceHistory(teamId, query);
        return ResponseEntity.ok(ApiResponse.success("Team attendance history retrieved successfully", response));
    }

    // ── Department Attendance Endpoints ─────────────────────────────────────

    @Operation(summary = "Get Department Daily Attendance", description = "Returns department daily attendance rollup, team-wise distribution, and employee attendance details for a specific date.")
    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<ApiResponse<DepartmentDailyAttendanceResponse>> getDepartmentDailyAttendance(
            @PathVariable("departmentId") Long departmentId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "status", required = false) AttendanceStatus status) {

        DepartmentDailyAttendanceResponse response = departmentAttendanceService.getDepartmentDailyAttendance(departmentId, date, status);
        return ResponseEntity.ok(ApiResponse.success("Department daily attendance retrieved successfully", response));
    }

    @Operation(summary = "Get Department Attendance History", description = "Returns paginated attendance history for all employees in a department with optional date range and status filters.")
    @GetMapping("/departments/{departmentId}/history")
    public ResponseEntity<ApiResponse<Page<TeamDepartmentAttendanceHistoryItemDto>>> getDepartmentAttendanceHistory(
            @PathVariable("departmentId") Long departmentId,
            @ModelAttribute DepartmentAttendanceHistoryQuery query) {

        Page<TeamDepartmentAttendanceHistoryItemDto> response = departmentAttendanceService.getDepartmentAttendanceHistory(departmentId, query);
        return ResponseEntity.ok(ApiResponse.success("Department attendance history retrieved successfully", response));
    }
}
