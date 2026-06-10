package com.example.ems.controller;

import com.example.ems.dto.EmployeeRequest;
import com.example.ems.dto.ApiResponse;
import com.example.ems.dto.ErrorResponse;
import com.example.ems.entity.Employee;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.EmployeeService;
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
    private com.example.ems.repository.EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private com.example.ems.service.AttendanceService attendanceService;

    @Autowired
    private com.example.ems.service.LeaveService leaveService;

    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid EmployeeRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.create' permission.", "AUTH_002"));
        }

        try {
            Employee created = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EMP_001"));
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employees list retrieved successfully", employeeService.getAllEmployees()));
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<?> getEmployeeById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        return employeeService.getEmployeeById(id)
                .<ResponseEntity<?>>map(emp -> ResponseEntity.ok(ApiResponse.success("Employee details retrieved successfully", emp)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002")));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid EmployeeRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.write")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.write' permission.", "AUTH_002"));
        }

        try {
            return employeeService.updateEmployee(id, request)
                    .<ResponseEntity<?>>map(emp -> ResponseEntity.ok(ApiResponse.success("Employee updated successfully", emp)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EMP_003"));
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<?> deleteEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.delete' permission.", "AUTH_002"));
        }

        boolean deleted = employeeService.deleteEmployee(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }
    }

    @GetMapping("/employees/department/{departmentId}")
    public ResponseEntity<?> getEmployeesByDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("departmentId") String departmentId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        java.util.List<Employee> list = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Employees for department retrieved successfully", list));
    }

    @GetMapping("/employees/manager/{managerId}")
    public ResponseEntity<?> getEmployeesByManager(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("managerId") Long managerId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        java.util.List<Employee> list = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(ApiResponse.success("Employees for manager retrieved successfully", list));
    }

    // ── 7. Search Employees ──────────────────────────────────────────────────
    @GetMapping("/employees/search")
    public ResponseEntity<?> searchEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "query", required = false) String query) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        java.util.List<Employee> results = employeeService.searchEmployees(query);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }

    // ── 8. Get Documents ─────────────────────────────────────────────────────
    @GetMapping("/employees/{id}/documents")
    public ResponseEntity<?> getEmployeeDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        // Check permission: HR/Admin or self
        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this employee's documents.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employee documents list retrieved", 
                employeeDocumentRepository.findByEmployeeId(id)));
    }

    // ── 9. Upload Document (Simulated) ──────────────────────────────────────
    @PostMapping("/employees/{id}/documents")
    public ResponseEntity<?> uploadEmployeeDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.write");
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot upload documents for this employee.", "AUTH_002"));
        }

        String fileName = (String) body.getOrDefault("fileName", "document.pdf");
        String fileType = (String) body.getOrDefault("fileType", "application/pdf");
        Long fileSize = ((Number) body.getOrDefault("fileSize", 1024L)).longValue();

        com.example.ems.entity.EmployeeDocument doc = new com.example.ems.entity.EmployeeDocument();
        doc.setEmployee(employee);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileSize(fileSize);
        doc.setDownloadUrl("http://localhost:8080/api/documents/download/" + System.currentTimeMillis());
        doc.setUploadedAt(java.time.LocalDateTime.now());

        com.example.ems.entity.EmployeeDocument saved = employeeDocumentRepository.save(doc);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully (Simulated)", saved));
    }

    // ── 10. Get Salary Details ───────────────────────────────────────────────
    @GetMapping("/employees/{id}/salary")
    public ResponseEntity<?> getEmployeeSalary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
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
    public ResponseEntity<?> getEmployeeAttendanceSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this employee's attendance summary.", "AUTH_002"));
        }

        java.util.List<com.example.ems.entity.Attendance> list = attendanceService.getAttendanceByEmployeeId(id);
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
    public ResponseEntity<?> getEmployeeLeaveSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(employee.getEmail());
        boolean hasAccess = isSelf || roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this employee's leave summary.", "AUTH_002"));
        }

        java.util.Map<String, Object> balance = leaveService.getLeaveBalance(id);

        java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("employeeId", employee.getId());
        summary.put("leaveBalances", balance);

        return ResponseEntity.ok(ApiResponse.success("Leave summary retrieved successfully", summary));
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
