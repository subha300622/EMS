package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.AttendanceRequest;
import com.example.ems.attendance.dto.CheckInRequest;
import com.example.ems.attendance.dto.CheckOutRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private RoleService roleService;

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
    @PostMapping("/attendance/me/check-in")
    public ResponseEntity<?> checkIn(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestBody(required = false) CheckInRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found", "EMP_002"));
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
    @PostMapping("/attendance/me/check-out")
    public ResponseEntity<?> checkOut(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestBody(required = false) CheckOutRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        try {
            String notes = request != null ? request.getNotes() : null;
            Attendance record = attendanceService.checkOut(employee, notes);
            return ResponseEntity.ok(ApiResponse.success("Checked out successfully", record));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_002"));
        }
    }


    // ── 4. GET MY ATTENDANCE HISTORY ─────────────────────────────────────────
    @GetMapping("/attendance/me")
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
    @PutMapping({"/attendance/{id}/correct", "/attendance/{id}"})
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
            return ResponseEntity.ok(ApiResponse.success("Attendance record updated successfully", record));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ATT_004"));
        }
    }

    @DeleteMapping("/attendance/{id}")
    public ResponseEntity<?> deleteAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

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
            attendanceService.deleteAttendanceRecord(id);
            return ResponseEntity.ok(ApiResponse.success("Attendance record deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ATT_003"));
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

    // ── 11. GET ATTENDANCE DASHBOARD ──────────────────────────────────────────
    @GetMapping("/attendance/dashboard")
    public ResponseEntity<?> getAttendanceDashboard(
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

        List<Attendance> todayRecords = attendanceService.getTodayAllAttendance();
        long present = todayRecords.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        long late = todayRecords.stream().filter(a -> "LATE".equalsIgnoreCase(a.getStatus())).count();
        long absent = todayRecords.stream().filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus())).count();
        long halfDay = todayRecords.stream().filter(a -> "HALF_DAY".equalsIgnoreCase(a.getStatus())).count();

        java.util.Map<String, Object> dash = new java.util.LinkedHashMap<>();
        dash.put("totalCheckedInToday", todayRecords.size());
        dash.put("present", present);
        dash.put("late", late);
        dash.put("absent", absent);
        dash.put("halfDay", halfDay);

        return ResponseEntity.ok(ApiResponse.success("Attendance dashboard retrieved", dash));
    }

    // ── 12. GET MONTHLY ATTENDANCE GRID ───────────────────────────────────────
    @GetMapping("/attendance/monthly")
    public ResponseEntity<?> getMonthlyAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

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

        int targetMonth = month != null ? month : java.time.LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : java.time.LocalDate.now().getYear();

        List<Attendance> all = attendanceService.getAllAttendanceRecords().stream()
                .filter(a -> a.getDate().getMonthValue() == targetMonth && a.getDate().getYear() == targetYear)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Monthly attendance records retrieved", all));
    }

    // ── 13. EXPORT ATTENDANCE ────────────────────────────────────────────────
    @GetMapping("/attendance/export")
    public ResponseEntity<?> exportAttendance(
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

        List<Attendance> list = attendanceService.getAllAttendanceRecords();
        StringBuilder csv = new StringBuilder("ID,Employee Name,Date,Status,Punch In,Punch Out,Notes\n");
        for (Attendance a : list) {
            csv.append(a.getId()).append(",")
               .append(a.getEmployee().getFullName()).append(",")
               .append(a.getDate()).append(",")
               .append(a.getStatus()).append(",")
               .append(a.getPunchInTime() != null ? a.getPunchInTime() : "").append(",")
               .append(a.getPunchOutTime() != null ? a.getPunchOutTime() : "").append(",")
               .append(a.getNotes() != null ? a.getNotes() : "").append("\n");
        }

        byte[] data = csv.toString().getBytes();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "attendance.csv");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
