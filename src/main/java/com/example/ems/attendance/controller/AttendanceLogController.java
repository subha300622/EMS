package com.example.ems.attendance.controller;

import com.example.ems.attendance.entity.AttendanceLog;
import com.example.ems.attendance.service.AttendanceLogService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Attendance Logs Management")
public class AttendanceLogController {

    @Autowired
    private AttendanceLogService attendanceLogService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleService roleService;

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

    @Operation(summary = "Get Employee Daily Swipe Logs", description = "Retrieves paginated daily swipe logs for a specific employee.")
    @GetMapping("/attendance/employee/{employeeId}/logs")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Page<AttendanceLog>>> getDailyLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee currentEmployee = resolveEmployee(currentUser);
        if (currentEmployee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found", "EMP_002"));
        }

        // Standard user can only query their own logs. Admin/HR can query anyone's logs.
        boolean isSelf = currentEmployee.getId().equals(employeeId);
        boolean hasPermission = roleService.hasPermission(currentUser.getWorkEmail(), "attendance.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "attendance.manage");

        if (!isSelf && !hasPermission) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You can only view your own logs.", "AUTH_002"));
        }

        LocalDate targetDate = date != null ? date : LocalDate.now();
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceLog> logs = attendanceLogService.getDailyLogs(employeeId, targetDate, pageable);

        return ResponseEntity.ok(ApiResponse.success("Swipe logs retrieved successfully", logs));
    }
}
