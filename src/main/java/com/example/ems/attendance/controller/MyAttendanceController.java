package com.example.ems.attendance.controller;

import java.util.List;
import com.example.ems.attendance.dto.CheckInRequest;
import com.example.ems.attendance.dto.CheckOutRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Attendance")
public class MyAttendanceController {

    @Autowired
    private AttendanceService attendanceService;

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
        if (currentUser == null) return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    // ── 1. CHECK-IN ──────────────────────────────────────────────────────────
    @Operation(summary = "Check In", description = "Records the daily check-in/punch-in time and optional notes for the employee.")
    @PostMapping("/attendance/me/check-in")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> checkIn(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestBody(required = false) CheckInRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId).orElse(null);
        }

        if (employee == null) {
            employee = resolveEmployee(currentUser);
        }

        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            String notes = request != null ? request.getNotes() : null;
            Attendance record = attendanceService.checkIn(employee, notes);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Checked in successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_001"));
        }
    }

    // ── 2. CHECK-OUT ─────────────────────────────────────────────────────────
    @Operation(summary = "Check Out", description = "Records the daily check-out/punch-out time and optional notes for the employee.")
    @PostMapping("/attendance/me/check-out")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> checkOut(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestBody(required = false) CheckOutRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId).orElse(null);
        }

        if (employee == null) {
            employee = resolveEmployee(currentUser);
        }

        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            String notes = request != null ? request.getNotes() : null;
            Attendance record = attendanceService.checkOut(employee, notes);
            return ResponseEntity.ok(ApiResponse.success("Checked out successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_002"));
        }
    }

    // ── 4. GET MY ATTENDANCE HISTORY ─────────────────────────────────────────
    @Operation(summary = "Get My Attendance History", description = "Retrieves the logged-in employee's complete attendance logs.")
    @GetMapping("/attendance/me")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Attendance>>> getMyAttendanceHistory(
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

        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved successfully", 
                attendanceService.getAttendanceByEmployeeId(employee.getId())));
    }
}
