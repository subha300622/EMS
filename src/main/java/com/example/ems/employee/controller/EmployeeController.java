package com.example.ems.employee.controller;
import java.util.List;
import com.example.ems.payroll.entity.Payroll;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.EmployeeService;

import com.example.ems.leave.service.LeaveService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Employee Management")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private com.example.ems.payroll.service.PayrollService payrollService;

    @PostMapping("/employees")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid EmployeeRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.create")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.create' permission.", "AUTH_002"));
        }

        try {
            Employee created = employeeService.createEmployee(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee created successfully", created));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EMP_001"));
        }
    }

    @GetMapping("/employees")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employees list retrieved successfully", employeeService.getAllEmployees()));
    }

    @GetMapping("/employees/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        return (ResponseEntity) employeeService.getEmployeeById(id)
                .<ResponseEntity<?>>map(emp -> ResponseEntity.ok(ApiResponse.success("Employee details retrieved successfully", emp)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002")));
    }

    @PutMapping("/employees/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ErrorResponse> updateEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid EmployeeRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.write")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.write' permission.", "AUTH_002"));
        }

        try {
            return (ResponseEntity) employeeService.updateEmployee(id, request)
                    .<ResponseEntity<?>>map(emp -> ResponseEntity.ok(ApiResponse.success("Employee updated successfully", emp)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002")));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EMP_003"));
        }
    }

    @DeleteMapping("/employees/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.delete")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.delete' permission.", "AUTH_002"));
        }

        boolean deleted = employeeService.deleteEmployee(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully"));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }
    }

    @GetMapping("/employees/department/{departmentId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeesByDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("departmentId") String departmentId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        java.util.List<Employee> list = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Employees for department retrieved successfully", list));
    }

    @GetMapping("/employees/manager/{managerId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeesByManager(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("managerId") Long managerId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        java.util.List<Employee> list = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(ApiResponse.success("Employees for manager retrieved successfully", list));
    }


    // ── 10. Get Salary Details ───────────────────────────────────────────────
    @GetMapping("/employees/{id}/salary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeSalary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this employee's salary details.", "AUTH_002"));
        }

        java.math.BigDecimal monthly = java.math.BigDecimal.ZERO;
        if (employee.getAnnualSalary() != null) {
            monthly = employee.getAnnualSalary().divide(java.math.BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        }

        java.util.Map<String, Object> salaryDetails = new java.util.LinkedHashMap<>();
        salaryDetails.put("employeeId", employee.getId());
        salaryDetails.put("annualSalary", employee.getAnnualSalary());
        salaryDetails.put("monthlyBaseSalary", monthly);

        return ResponseEntity.ok(ApiResponse.success("Salary details retrieved", salaryDetails));
    }

    // ── 11. Get Attendance Summary ───────────────────────────────────────────
    @GetMapping("/employees/{id}/attendance-summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeAttendanceSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this employee's attendance summary.", "AUTH_002"));
        }

        java.util.List<Attendance> list = attendanceService.getAttendanceByEmployeeId(id);
        long present = list.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        long late = list.stream().filter(a -> "LATE".equalsIgnoreCase(a.getStatus())).count();
        long absent = list.stream().filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus())).count();
        long halfday = list.stream().filter(a -> "HALF_DAY".equalsIgnoreCase(a.getStatus())).count();

        java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("employeeId", employee.getId());
        summary.put("totalDaysCounted", list.size());
        summary.put("present", present);
        summary.put("late", late);
        summary.put("absent", absent);
        summary.put("halfDay", halfday);

        return ResponseEntity.ok(ApiResponse.success("Attendance summary retrieved successfully", summary));
    }

    // ── 12. Get Leave Summary ────────────────────────────────────────────────
    @GetMapping("/employees/{id}/leave-summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeeLeaveSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this employee's leave summary.", "AUTH_002"));
        }

        java.util.Map<String, Object> balance = leaveService.getLeaveBalance(id);

        java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("employeeId", employee.getId());
        summary.put("leaveBalances", balance);

        return ResponseEntity.ok(ApiResponse.success("Leave summary retrieved successfully", summary));
    }

    @PatchMapping("/employees/{id}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ErrorResponse> updateEmployeeStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid com.example.ems.auth.dto.UpdateStatusRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.write")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.write' permission.", "AUTH_002"));
        }

        try {
            return (ResponseEntity) employeeService.updateEmployeeStatus(id, request.getStatus())
                    .<ResponseEntity<?>>map(emp -> ResponseEntity.ok(ApiResponse.success("Employee status updated successfully", emp)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002")));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EMP_003"));
        }
    }


    @GetMapping("/employees/{id}/payroll")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Payroll>>> getEmployeePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this employee's payroll details.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employee payroll list retrieved successfully",
                payrollService.getPayrollByEmployeeId(id)));
    }

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
}
