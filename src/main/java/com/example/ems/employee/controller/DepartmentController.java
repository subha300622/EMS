package com.example.ems.employee.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.DepartmentCreateRequest;
import com.example.ems.employee.dto.DepartmentResponseDto;
import com.example.ems.employee.dto.DepartmentUpdateRequest;
import com.example.ems.employee.entity.DepartmentAuditLog;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.service.DepartmentService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Department Management")
public class DepartmentController {

        @Autowired
        private DepartmentService departmentService;

        @Autowired
        private UserRepository userRepository;

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

        private boolean isPlatformAdmin(User user) {
                return user != null && user.getRole() != null && "PLATFORM_ADMIN".equalsIgnoreCase(user.getRole().getName());
        }

        @Operation(summary = "Create Department", description = "Creates a new department within the authenticated user's organization.")
        @PostMapping("/departments")
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ResponseEntity<ApiResponse<Department>> createDepartment(
                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                        @RequestBody DepartmentCreateRequest request) {

                User currentUser = resolveUser(authHeader);
                if (currentUser == null) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
                }
                if (isPlatformAdmin(currentUser)) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Platform Admin cannot perform organization domain mutations", "AUTH_003"));
                }
                if (!roleService.hasPermission(currentUser.getWorkEmail(), "department.create") &&
                    !(currentUser.getRole() != null && "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()))) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Access Denied: Requires 'department.create' permission.", "AUTH_002"));
                }

                try {
                        Department created = departmentService.createDepartment(request, currentUser);
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success("Department created successfully", created));
                } catch (IllegalArgumentException e) {
                        return (ResponseEntity) ResponseEntity.badRequest()
                                        .body(ErrorResponse.error(e.getMessage(), "DEP_001"));
                }
        }

        @Operation(summary = "Get All Departments", description = "Retrieves a detailed list of departments belonging to the authenticated organization context.")
        @GetMapping("/departments")
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ResponseEntity<ApiResponse<List<DepartmentResponseDto>>> getDepartments(
                        @RequestHeader(value = "Authorization", required = false) String authHeader) {

                User currentUser = resolveUser(authHeader);
                if (currentUser == null) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
                }

                List<DepartmentResponseDto> response = departmentService.getDepartmentsList();
                return ResponseEntity.ok(ApiResponse.success("Departments list retrieved successfully", response));
        }

        @Operation(summary = "Get Department Details", description = "Retrieves detailed department profile for the authenticated organization.")
        @GetMapping("/departments/{id}")
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ResponseEntity<ApiResponse<DepartmentResponseDto>> getDepartmentById(
                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                        @PathVariable("id") Long id) {

                User currentUser = resolveUser(authHeader);
                if (currentUser == null) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
                }

                return departmentService.getDepartmentDetails(id)
                                .map(details -> ResponseEntity.ok(ApiResponse
                                                .success("Department details retrieved successfully", details)))
                                .orElseGet(() -> (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ErrorResponse.error("Department not found in your organization", "DEP_404")));
        }

        @Operation(summary = "Update Department", description = "Updates a department within the user's organization and records audit history.")
        @PutMapping("/departments/{id}")
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ResponseEntity<ApiResponse<DepartmentResponseDto>> updateDepartment(
                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                        @PathVariable("id") Long id,
                        @RequestBody DepartmentUpdateRequest request) {

                User currentUser = resolveUser(authHeader);
                if (currentUser == null) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
                }
                if (isPlatformAdmin(currentUser)) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Platform Admin cannot perform organization domain mutations", "AUTH_003"));
                }
                if (!roleService.hasPermission(currentUser.getWorkEmail(), "department.update") &&
                    !(currentUser.getRole() != null && "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()))) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Access Denied: Requires 'department.update' permission.", "AUTH_002"));
                }

                try {
                        DepartmentResponseDto updated = departmentService.updateDepartment(id, request, currentUser);
                        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
                } catch (IllegalArgumentException e) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ErrorResponse.error(e.getMessage(), "DEP_404"));
                }
        }

        @Operation(summary = "Get Department Change History", description = "Retrieves the audit log history of changes for a department.")
        @GetMapping("/departments/{id}/history")
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ResponseEntity<ApiResponse<List<DepartmentAuditLog>>> getHistory(
                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                        @PathVariable("id") Long id) {

                User currentUser = resolveUser(authHeader);
                if (currentUser == null) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
                }

                List<DepartmentAuditLog> history = departmentService.getDepartmentHistory(id);
                return ResponseEntity
                                .ok(ApiResponse.success("Department change history retrieved successfully", history));
        }

        @Operation(summary = "Delete Department", description = "Deletes a department belonging to the authenticated organization.")
        @DeleteMapping("/departments/{id}")
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ResponseEntity<Object> deleteDepartment(
                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                        @PathVariable("id") Long id) {

                User currentUser = resolveUser(authHeader);
                if (currentUser == null) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
                }
                if (isPlatformAdmin(currentUser)) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Platform Admin cannot perform organization domain mutations", "AUTH_003"));
                }
                if (!roleService.hasPermission(currentUser.getWorkEmail(), "department.delete") &&
                    !(currentUser.getRole() != null && "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()))) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Access Denied: Requires 'department.delete' permission.", "AUTH_002"));
                }

                boolean deleted = departmentService.deleteDepartment(id);
                if (deleted) {
                        Map<String, String> response = new LinkedHashMap<>();
                        response.put("id", String.valueOf(id));
                        return ResponseEntity.ok(response);
                } else {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ErrorResponse.error("Department not found in your organization", "DEP_404"));
                }
        }

        @Operation(summary = "Toggle Department Status", description = "Deactivates or activates a department status in the authenticated organization.")
        @PatchMapping("/departments/{id}")
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ResponseEntity<ApiResponse<Map<String, String>>> toggleStatus(
                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                        @PathVariable("id") Long id,
                        @RequestBody Map<String, String> statusMap) {

                User currentUser = resolveUser(authHeader);
                if (currentUser == null) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
                }
                if (isPlatformAdmin(currentUser)) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Platform Admin cannot perform organization domain mutations", "AUTH_003"));
                }
                if (!roleService.hasPermission(currentUser.getWorkEmail(), "department.update") &&
                    !(currentUser.getRole() != null && "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()))) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ErrorResponse.error("Access Denied: Requires 'department.update' permission.", "AUTH_002"));
                }

                String status = statusMap.get("status");
                if (status == null || (!status.equalsIgnoreCase("Active") && !status.equalsIgnoreCase("Inactive"))) {
                        return (ResponseEntity) ResponseEntity.badRequest()
                                        .body(ErrorResponse.error("Invalid status value", "DEP_400"));
                }

                try {
                        Department updated = departmentService.toggleDepartmentStatus(id, status);
                        Map<String, String> response = new LinkedHashMap<>();
                        response.put("id", String.valueOf(updated.getId()));
                        response.put("status", status.equalsIgnoreCase("Active") ? "Active" : "Inactive");
                        return ResponseEntity
                                        .ok(ApiResponse.success("Department status updated successfully", response));
                } catch (IllegalArgumentException e) {
                        return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ErrorResponse.error(e.getMessage(), "DEP_404"));
                }
        }
}
