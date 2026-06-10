package com.example.ems.controller;

import com.example.ems.dto.AttendanceRequest;
import com.example.ems.dto.ApiResponse;
import com.example.ems.dto.ErrorResponse;
import com.example.ems.entity.Attendance;
import com.example.ems.entity.Employee;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.AttendanceService;
import com.example.ems.service.JwtService;
import com.example.ems.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.ems.repository.EmployeeRepository employeeRepository;

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
    @PostMapping("/attendance/check-in")
    public ResponseEntity<?> checkIn(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) com.example.ems.dto.CheckInRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        try {
            String notes = request != null ? request.getNotes() : null;
            Attendance record = attendanceService.checkIn(employee, notes);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Checked in successfully", record));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_001"));
        }
    }

    // ── 2. CHECK-OUT ─────────────────────────────────────────────────────────
    @PostMapping("/attendance/check-out")
    public ResponseEntity<?> checkOut(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) com.example.ems.dto.CheckOutRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        try {
            String notes = request != null ? request.getNotes() : null;
            Attendance record = attendanceService.checkOut(employee, notes);
            return ResponseEntity.ok(ApiResponse.success("Checked out successfully", record));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_002"));
        }
    }

    // ── 3. GET MY ATTENDANCE FOR TODAY ───────────────────────────────────────
    @GetMapping("/attendance/my")
    public ResponseEntity<?> getMyTodayAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        return attendanceService.getTodayAttendance(employee)
                .<ResponseEntity<?>>map(record -> ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved", record)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success("No attendance record for today", null)));
    }

    // ── 4. GET MY ATTENDANCE HISTORY ─────────────────────────────────────────
    @GetMapping("/attendance/my/history")
    public ResponseEntity<?> getMyAttendanceHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved successfully", 
                attendanceService.getAttendanceByEmployeeId(employee.getId())));
    }

    // ── 5. GET ALL ATTENDANCE RECORDS (ADMIN / HR) ───────────────────────────
    @GetMapping("/attendance")
    public ResponseEntity<?> getAllAttendanceRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.read' or 'attendance.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", 
                attendanceService.getAllAttendanceRecords()));
    }

    // ── 6. GET ATTENDANCE BY ID (ADMIN / HR) ──────────────────────────────────
    @GetMapping("/attendance/{id}")
    public ResponseEntity<?> getAttendanceById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.read' or 'attendance.manage' permission.", "AUTH_002"));
        }

        // Using findById via service/repository
        return attendanceService.getAllAttendanceRecords().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .<ResponseEntity<?>>map(record -> ResponseEntity.ok(ApiResponse.success("Attendance record retrieved successfully", record)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Attendance record not found with ID: " + id, "ATT_003")));
    }

    // ── 7. GET ATTENDANCE BY EMPLOYEE ID (ADMIN / HR) ─────────────────────────
    @GetMapping("/attendance/employee/{employeeId}")
    public ResponseEntity<?> getAttendanceByEmployeeId(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "attendance.team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.read', 'attendance.manage', or 'attendance.team.read' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employee attendance records retrieved successfully", 
                attendanceService.getAttendanceByEmployeeId(employeeId)));
    }

    // ── 8. CORRECT ATTENDANCE (ADMIN / HR) ────────────────────────────────────
    @PutMapping("/attendance/{id}/correct")
    public ResponseEntity<?> correctAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid AttendanceRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.manage' permission.", "AUTH_002"));
        }

        try {
            Attendance record = attendanceService.updateAttendanceRecord(id, request);
            return ResponseEntity.ok(ApiResponse.success("Attendance record corrected successfully", record));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_004"));
        }
    }

    // ── 9. GET TODAY'S ATTENDANCE RECORDS (ADMIN / HR) ────────────────────────
    @GetMapping("/attendance/today")
    public ResponseEntity<?> getTodayAttendanceRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.read' or 'attendance.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Today's attendance records retrieved successfully", 
                attendanceService.getTodayAllAttendance()));
    }

    // ── 10. GET STATS ─────────────────────────────────────────────────────────
    @GetMapping("/attendance/stats")
    public ResponseEntity<?> getAttendanceStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "attendance.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'attendance.read' or 'attendance.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Attendance stats retrieved successfully", 
                attendanceService.getAttendanceStats(employeeId)));
    }
}
