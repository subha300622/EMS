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
