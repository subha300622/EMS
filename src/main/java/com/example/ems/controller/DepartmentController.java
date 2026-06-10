package com.example.ems.controller;

import com.example.ems.dto.ApiResponse;
import com.example.ems.dto.ErrorResponse;
import com.example.ems.dto.DepartmentRequest;
import com.example.ems.entity.Department;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.JwtService;
import com.example.ems.service.DepartmentService;
import com.example.ems.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

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

    // ── 1. GET ALL DEPARTMENTS ───────────────────────────────────────────────
    @GetMapping("/departments")
    public ResponseEntity<?> getAllDepartments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully",
                departmentService.getAllDepartments()));
    }

    // ── 2. GET DEPARTMENT BY ID ──────────────────────────────────────────────
    @GetMapping("/departments/{id}")
    public ResponseEntity<?> getDepartmentById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return departmentService.getDepartmentById(id)
                .<ResponseEntity<?>>map(
                        d -> ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", d)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Department not found with ID: " + id, "DEP_001")));
    }

    // ── 3. CREATE DEPARTMENT (ADMIN / HR) ────────────────────────────────────
    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid DepartmentRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        // Requires department management permissions (e.g. employee.create/update or
        // department.manage)
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.create")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "department.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires creation permissions.", "AUTH_002"));
        }

        try {
            Department d = departmentService.createDepartment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Department created successfully", d));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DEP_002"));
        }
    }

    // ── 4. UPDATE DEPARTMENT (ADMIN / HR) ────────────────────────────────────
    @PutMapping("/departments/{id}")
    public ResponseEntity<?> updateDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid DepartmentRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "department.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires update permissions.", "AUTH_002"));
        }

        try {
            Department d = departmentService.updateDepartment(id, request);
            return ResponseEntity.ok(ApiResponse.success("Department updated successfully", d));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DEP_003"));
        }
    }

    // ── 5. DELETE DEPARTMENT (ADMIN) ─────────────────────────────────────────
    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "department.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'department.manage' permission.", "AUTH_002"));
        }

        boolean deleted = departmentService.deleteDepartment(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Department not found with ID: " + id, "DEP_001"));
        }
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }
}
