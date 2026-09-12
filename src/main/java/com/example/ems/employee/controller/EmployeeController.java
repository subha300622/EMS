package com.example.ems.employee.controller;

import java.util.List;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.EmployeeService;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.entity.Department;
import java.util.HashMap;

import com.example.ems.security.service.JwtService;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Employees")
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
    private DepartmentRepository departmentRepository;

    @Operation(summary = "Create Employee Record", description = "Creates a new employee profile in the system with contact details, department, role, and salary parameters.")
    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid EmployeeRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'employee.create' permission.", "AUTH_002"));
        }

        try {

            Employee created = employeeService.createEmployee(request, currentUser.getWorkEmail());
            EmployeeCreatedResponseDto res = new EmployeeCreatedResponseDto(
                    created.getId(),
                    created.getEmployeeId(),
                    created.getOrganization() != null ? created.getOrganization().getId() : null,
                    created.getFullName(),
                    created.getEmail(),
                    created.getDepartment(),
                    created.getDesignation(),
                    created.getJoiningDate(),
                    created.getStatus()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee created successfully", res));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("PLATFORM_ADMIN cannot be assigned through employee role management.",
                                "PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE"));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(msg, "EMP_001"));
        }
    }

    @Operation(summary = "Get All Employees", description = "Retrieves a paginated, searchable, and filterable list of all employees in the system.")
    @GetMapping("/employees")
    @Transactional(readOnly = true)
public ResponseEntity<?> getAllEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read") &&
                !roleService.hasPermission(currentUser.getWorkEmail(), "employee.directory.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        List<Employee> all = employeeService.getAllEmployees();

        // Load departments and build department manager mapping to avoid N+1 queries
        List<Department> departments = departmentRepository.findAll();
        Map<Long, Employee> employeeMap = all.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e, (a, b) -> a));
        Map<String, Employee> deptManagerMap = new HashMap<>();
        for (Department d : departments) {
            if (d.getManagerId() != null) {
                Employee mgr = employeeMap.get(d.getManagerId());
                if (mgr != null) {
                    deptManagerMap.put(d.getName().toLowerCase(), mgr);
                }
            }
        }

        List<EmployeeListItemDto> filtered = all.stream()
                .filter(emp -> {
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase().trim();
                        boolean matches = emp.getFullName().toLowerCase().contains(s)
                                || (emp.getEmployeeId() != null && emp.getEmployeeId().toLowerCase().contains(s))
                                || (emp.getDesignation() != null && emp.getDesignation().toLowerCase().contains(s))
                                || (emp.getDepartment() != null && emp.getDepartment().toLowerCase().contains(s));
                        if (!matches)
                            return false;
                    }
                    if (department != null && !department.isBlank() && (emp.getDepartment() == null
                            || !emp.getDepartment().equalsIgnoreCase(department.trim()))) {
                        return false;
                    }
                    if (status != null && !status.isBlank()
                            && (emp.getStatus() == null || !emp.getStatus().equalsIgnoreCase(status.trim()))) {
                        return false;
                    }
                    return true;
                })
                .map(emp -> {
                    Employee manager = emp.getManager();
                    if (manager == null && emp.getDepartment() != null) {
                        manager = deptManagerMap.get(emp.getDepartment().toLowerCase());
                    }
                    return new EmployeeListItemDto(
                            emp.getId(),
                            emp.getEmployeeId(),
                            emp.getOrganization() != null ? emp.getOrganization().getId() : null,
                            emp.getFullName(),
                            emp.getDesignation(),
                            emp.getDepartment(),
                            emp.getStatus(),
                            emp.getWorkMode(),
                            manager != null ? manager.getId() : null,
                            manager != null ? manager.getFullName() : "Unassigned");
                })
                .collect(Collectors.toList());

        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0)
            totalPages = 1;
        int start = page * size;
        List<EmployeeListItemDto> content;
        if (start >= totalElements) {
            content = new ArrayList<>();
        } else {
            int end = Math.min(start + size, totalElements);
            content = filtered.subList(start, end);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeListItemDto> pageResult = new PageImpl<>(content, pageable, totalElements);

        return ResponseEntity.ok(ApiResponse.success("Employees list retrieved successfully", pageResult));
    }

    @Operation(summary = "Get Employee Master Profile", description = "Retrieves full master profile of an employee by employeeId.")
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<?> getEmployeeMasterProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        try {
            Map<String, Object> data = employeeService.getEmployeeMasterProfileData(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "EMP_002"));
        }
    }

    @Operation(summary = "Update Employee Master Profile", description = "Updates attributes of an existing employee master profile.")
    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<?> updateEmployeeMasterProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId,
            @RequestBody @Valid EmployeeRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'employee.update' permission.", "AUTH_002"));
        }

        try {
            Map<String, Object> data = employeeService.updateEmployeeMasterProfile(employeeId, request,
                    currentUser.getWorkEmail());
            EmployeeUpdateResponseDto res = new EmployeeUpdateResponseDto(
                    (String) data.get("employeeId"),
                    (String) data.get("fullName"),
                    (String) data.get("email"),
                    (String) data.get("employmentStatus")
            );
            return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "EMP_003"));
        }
    }

    @Operation(summary = "Get Employee Status", description = "Retrieves current employment and account status for an employee.")
    @GetMapping("/employees/{employeeId}/status")
    public ResponseEntity<?> getEmployeeStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        try {
            Map<String, Object> data = employeeService.getEmployeeStatusDetail(employeeId);
            EmployeeStatusResponseDto res = new EmployeeStatusResponseDto(
                    (String) data.get("employeeId"),
                    (String) data.get("status"),
                    (String) data.get("employmentStatus"),
                    (String) data.get("userAccountStatus")
            );
            return ResponseEntity.ok(ApiResponse.success("Employee status retrieved successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "EMP_002"));
        }
    }

    @Operation(summary = "Update Employee Status", description = "Updates status indicators for an employee.")
    @PatchMapping("/employees/{employeeId}/status")
    public ResponseEntity<?> updateEmployeeStatusPatch(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId,
            @RequestBody @Valid UpdateEmployeeStatusRequest body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'employee.update' permission.", "AUTH_002"));
        }

        String newStatus = body != null ? body.status() : null;
        String reason = body != null ? body.reason() : null;
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Status field is required", "VAL_001"));
        }

        try {
            Map<String, Object> data = employeeService.updateEmployeeStatusPatch(employeeId, newStatus, reason,
                    currentUser);
            EmployeeStatusResponseDto res = new EmployeeStatusResponseDto(
                    (String) data.get("employeeId"),
                    (String) data.get("status"),
                    (String) data.get("status"),
                    null
            );
            return ResponseEntity.ok(ApiResponse.success("Employee status updated successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "EMP_003"));
        }
    }

    @Operation(summary = "Soft Delete Employee", description = "Soft deactivates an employee record and revokes user sessions.")
    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<?> deleteEmployeeMaster(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'employee.delete' permission.", "AUTH_002"));
        }

        try {
            Map<String, Object> data = employeeService.softDeleteEmployeeByIdentifier(employeeId, currentUser);
            EmployeeStatusResponseDto res = new EmployeeStatusResponseDto(
                    (String) data.get("employeeId"),
                    (String) data.get("status"),
                    (String) data.get("status"),
                    null
            );
            return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", res));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("The last Super Admin of an organization cannot be removed.",
                                "LAST_SUPER_ADMIN_CANNOT_BE_REMOVED"));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(msg, "EMP_004"));
        }
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

    // ── Employee Role Management Endpoints ─────────────────────────────────────

    @Operation(summary = "Get Employee Roles", description = "Retrieves active organization role assignments for an employee.")
    @GetMapping("/employees/{employeeId}/roles")
public ResponseEntity<?> getEmployeeRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.read' permission.", "AUTH_002"));
        }

        try {
            EmployeeRolesResponse res = employeeService.getEmployeeRoles(employeeId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Employee roles retrieved successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EMP_ROLE_001"));
        }
    }

    @Operation(summary = "Assign Single Role", description = "Assigns an organization role to an employee.")
    @PostMapping("/employees/{employeeId}/roles")
public ResponseEntity<?> assignRoleToEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody @Valid AssignRoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role assignment permission.", "AUTH_002"));
        }

        try {
            EmployeeRolesResponse res = employeeService.assignRoleToEmployee(employeeId, request.getRoleId(),
                    currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Role assigned successfully", res));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("PLATFORM_ADMIN cannot be assigned through employee role management.",
                                "PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE"));
            }
            if (msg != null && msg.startsWith("ROLE_ALREADY_ASSIGNED")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("One or more roles are already assigned to the employee.",
                                "ROLE_ALREADY_ASSIGNED"));
            }
            return ResponseEntity.badRequest().body(ErrorResponse.error(msg, "EMP_ROLE_002"));
        }
    }

    @Operation(summary = "Assign Multiple Roles", description = "Assigns multiple organization roles to an employee transactionally.")
    @PostMapping("/employees/{employeeId}/roles/bulk")
public ResponseEntity<?> assignBulkRolesToEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody @Valid AssignBulkRolesRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role assignment permission.", "AUTH_002"));
        }

        try {
            EmployeeRolesResponse res = employeeService.assignBulkRolesToEmployee(employeeId, request.getRoleIds(),
                    currentUser);
            return ResponseEntity.ok(ApiResponse.success("Bulk roles assigned successfully", res));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("PLATFORM_ADMIN cannot be assigned through employee role management.",
                                "PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE"));
            }
            if (msg != null && msg.startsWith("ROLE_ALREADY_ASSIGNED")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("One or more roles are already assigned to the employee.",
                                "ROLE_ALREADY_ASSIGNED"));
            }
            return ResponseEntity.badRequest().body(ErrorResponse.error(msg, "EMP_ROLE_003"));
        }
    }

    @Operation(summary = "Change Employee Roles", description = "Replaces/updates employee role assignments with effective date and reason.")
    @PutMapping("/employees/{employeeId}/roles")
public ResponseEntity<?> changeEmployeeRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody @Valid ChangeEmployeeRoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role assignment permission.", "AUTH_002"));
        }

        try {
            EmployeeRolesResponse res = employeeService.changeEmployeeRoles(employeeId, request, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Employee roles changed successfully", res));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("The last Super Admin of an organization cannot be removed.",
                                "LAST_SUPER_ADMIN_CANNOT_BE_REMOVED"));
            }
            if (msg != null && msg.startsWith("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("PLATFORM_ADMIN cannot be assigned through employee role management.",
                                "PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE"));
            }
            return ResponseEntity.badRequest().body(ErrorResponse.error(msg, "EMP_ROLE_004"));
        }
    }

    @Operation(summary = "Remove Employee Role", description = "Removes an assigned role from an employee.")
    @DeleteMapping("/employees/{employeeId}/roles/{roleId}")
public ResponseEntity<?> removeEmployeeRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @PathVariable Long roleId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role assignment permission.", "AUTH_002"));
        }

        try {
            EmployeeRolesResponse res = employeeService.removeEmployeeRole(employeeId, roleId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Role removed successfully", res));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("The last Super Admin of an organization cannot be removed.",
                                "LAST_SUPER_ADMIN_CANNOT_BE_REMOVED"));
            }
            return ResponseEntity.badRequest().body(ErrorResponse.error(msg, "EMP_ROLE_005"));
        }
    }

    @Operation(summary = "Get Assignable Roles", description = "Retrieves available organization roles for employee management screens.")
    @GetMapping("/roles/assignable")
public ResponseEntity<?> getAssignableRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<AssignableRoleDto> roles = employeeService.getAssignableRoles(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Assignable roles retrieved successfully", roles));
    }
}
