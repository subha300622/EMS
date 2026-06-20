package com.example.ems.reports.controller;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin("*")
@Tag(name = "Reports & Analytics")
public class DashboardReportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

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
        if (currentUser == null)
            return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    // ── 1. GET ADMIN DASHBOARD ────────────────────────────────────────────────
    @GetMapping("/dashboard/admin")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAdminDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasAccess = roleService.hasPermission(currentUser.getWorkEmail(), "reports.view")
                || roleService.hasPermission(currentUser.getWorkEmail(), "reports.hr")
                || roleService.hasRoleOrGreater(currentUser, "HR");

        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires admin/HR dashboard privileges.", "AUTH_002"));
        }

        Set<String> keys = redisTemplate.keys("session:user:*");
        long activeSessions = keys != null ? keys.size() : 0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("employeeCount", employeeRepository.count());
        stats.put("activeSessions", activeSessions);
        stats.put("departmentCount", departmentRepository.count());
        stats.put("pendingLeaves", leaveRepository.findByStatus("PENDING").size());
        stats.put("processedPayroll", payrollRepository.findAll().stream()
                .filter(p -> "PROCESSED".equalsIgnoreCase(p.getStatus())).count());

        return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved", stats));
    }

    // ── 2. GET MANAGER DASHBOARD ──────────────────────────────────────────────
    @GetMapping("/dashboard/manager")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getManagerDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for current user", "EMP_002"));
        }

        List<Employee> team = employeeRepository.findAll().stream()
                .filter(e -> e.getManager() != null && e.getManager().getId().equals(manager.getId()))
                .toList();

        long pendingLeaves = 0;
        long activeTeamCheckedIn = 0;
        java.time.LocalDate today = java.time.LocalDate.now();

        for (Employee member : team) {
            pendingLeaves += leaveRepository.findByEmployeeIdAndStatus(member.getId(), "PENDING").size();
            boolean checkedInToday = attendanceRepository.findByEmployeeId(member.getId()).stream()
                    .anyMatch(a -> today.equals(a.getDate()) && a.getPunchInTime() != null);
            if (checkedInToday) {
                activeTeamCheckedIn++;
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("teamSize", team.size());
        stats.put("pendingLeaves", pendingLeaves);
        stats.put("activeTeamCheckedIn", activeTeamCheckedIn);

        return ResponseEntity.ok(ApiResponse.success("Manager dashboard statistics retrieved", stats));
    }

    // ── 3. EXPORT ATTENDANCE REPORT ───────────────────────────────────────────
    @GetMapping("/reports/attendance")
    public ResponseEntity<?> exportAttendanceReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "reports.view")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "reports.hr")
                && !roleService.hasRole(currentUser, "SUPER_ADMIN")
                && !roleService.hasRole(currentUser, "ADMIN")
                && !roleService.hasRole(currentUser, "HR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires reports privileges.", "AUTH_002"));
        }

        List<Attendance> list = attendanceRepository.findAll();
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

        return createCsvResponse(csv.toString(), "attendance_report.csv");
    }

    // ── 4. EXPORT LEAVES REPORT ───────────────────────────────────────────────
    @GetMapping("/reports/leaves")
    public ResponseEntity<?> exportLeavesReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "reports.view")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "reports.hr")
                && !roleService.hasRole(currentUser, "SUPER_ADMIN")
                && !roleService.hasRole(currentUser, "ADMIN")
                && !roleService.hasRole(currentUser, "HR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires reports privileges.", "AUTH_002"));
        }

        List<Leave> list = leaveRepository.findAll();
        StringBuilder csv = new StringBuilder(
                "ID,Employee Name,Leave Type,Start Date,End Date,Status,Reason,Applied At\n");
        for (Leave l : list) {
            csv.append(l.getId()).append(",")
                    .append(l.getEmployee().getFullName()).append(",")
                    .append(l.getLeaveType() != null ? l.getLeaveType().getName() : "").append(",")
                    .append(l.getStartDate()).append(",")
                    .append(l.getEndDate()).append(",")
                    .append(l.getStatus()).append(",")
                    .append(l.getReason() != null ? l.getReason() : "").append(",")
                    .append(l.getAppliedAt()).append("\n");
        }

        return createCsvResponse(csv.toString(), "leaves_report.csv");
    }

    // ── 5. EXPORT EMPLOYEES REPORT ────────────────────────────────────────────
    @GetMapping("/reports/employees")
    public ResponseEntity<?> exportEmployeesReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "reports.view")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "reports.hr")
                && !roleService.hasRole(currentUser, "SUPER_ADMIN")
                && !roleService.hasRole(currentUser, "ADMIN")
                && !roleService.hasRole(currentUser, "HR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires reports privileges.", "AUTH_002"));
        }

        List<Employee> list = employeeRepository.findAll();
        StringBuilder csv = new StringBuilder(
                "ID,Employee ID,Full Name,Email,Phone,Department,Designation,Status,Joining Date\n");
        for (Employee e : list) {
            csv.append(e.getId()).append(",")
                    .append(e.getEmployeeId() != null ? e.getEmployeeId() : "").append(",")
                    .append(e.getFullName()).append(",")
                    .append(e.getEmail()).append(",")
                    .append(e.getPhone() != null ? e.getPhone() : "").append(",")
                    .append(e.getDepartment() != null ? e.getDepartment() : "").append(",")
                    .append(e.getDesignation() != null ? e.getDesignation() : "").append(",")
                    .append(e.getStatus()).append(",")
                    .append(e.getJoiningDate() != null ? e.getJoiningDate() : "").append("\n");
        }

        return createCsvResponse(csv.toString(), "employees_report.csv");
    }

    // ── 6. EXPORT PAYROLL REPORT ──────────────────────────────────────────────
    @GetMapping("/reports/payroll")
    public ResponseEntity<?> exportPayrollReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "reports.view")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "reports.finance")
                && !roleService.hasRole(currentUser, "SUPER_ADMIN")
                && !roleService.hasRole(currentUser, "ADMIN")
                && !roleService.hasRole(currentUser, "FINANCE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires payroll reports privileges.", "AUTH_002"));
        }

        List<Payroll> list = payrollRepository.findAll();
        StringBuilder csv = new StringBuilder(
                "ID,Employee ID,Employee Name,Month,Year,Basic Salary,Allowances,Deductions,Net Pay,Status\n");
        for (Payroll p : list) {
            csv.append(p.getId()).append(",")
                    .append(p.getEmployee().getEmployeeId() != null ? p.getEmployee().getEmployeeId() : "").append(",")
                    .append(p.getEmployee().getFullName()).append(",")
                    .append(p.getMonth()).append(",")
                    .append(p.getYear()).append(",")
                    .append(p.getBasicSalary()).append(",")
                    .append(p.getAllowances()).append(",")
                    .append(p.getDeductions()).append(",")
                    .append(p.getNetPay()).append(",")
                    .append(p.getStatus()).append("\n");
        }

        return createCsvResponse(csv.toString(), "payroll_report.csv");
    }

    private ResponseEntity<byte[]> createCsvResponse(String content, String fileName) {
        byte[] data = content.getBytes();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
