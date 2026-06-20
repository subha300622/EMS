package com.example.ems.employee.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.DepartmentRequest;
import com.example.ems.employee.dto.DepartmentTransferRequest;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.DepartmentTransfer;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.DepartmentService;
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
@Tag(name = "Organization Management")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private com.example.ems.employee.service.EmployeeService employeeService;

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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully",
                departmentService.getAllDepartments()));
    }

    // ── 2. GET DEPARTMENT BY ID ──────────────────────────────────────────────
    @GetMapping("/departments/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDepartmentById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return (ResponseEntity) departmentService.getDepartmentById(id)
                .<ResponseEntity<?>>map(
                        d -> ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", d)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Department not found with ID: " + id, "DEP_001")));
    }

    // ── 3. CREATE DEPARTMENT (ADMIN / HR) ────────────────────────────────────
    @PostMapping("/departments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid DepartmentRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.create")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "department.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires creation permissions.", "AUTH_002"));
        }

        try {
            Department d = departmentService.createDepartment(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Department created successfully", d));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DEP_002"));
        }
    }

    // ── 4. UPDATE DEPARTMENT (ADMIN / HR) ────────────────────────────────────
    @PutMapping("/departments/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid DepartmentRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "department.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires update permissions.", "AUTH_002"));
        }

        try {
            Department d = departmentService.updateDepartment(id, request);
            return ResponseEntity.ok(ApiResponse.success("Department updated successfully", d));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DEP_003"));
        }
    }

    // ── 5. DELETE DEPARTMENT (ADMIN) ─────────────────────────────────────────
    @DeleteMapping("/departments/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "department.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'department.manage' permission.", "AUTH_002"));
        }

        boolean deleted = departmentService.deleteDepartment(id);
        if (!deleted) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Department not found with ID: " + id, "DEP_001"));
        }
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }

    // ── 6. GET DEPARTMENTS DROPDOWN ──────────────────────────────────────────
    @GetMapping("/departments/dropdown")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDepartmentsDropdown(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<Map<String, Object>> dropdown = departmentService.getAllDepartments().stream()
                .map(d -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", d.getId());
                    map.put("name", d.getName());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Departments dropdown retrieved successfully", dropdown));
    }

    // ── 7. GET DEPARTMENTS HIERARCHY ─────────────────────────────────────────
    @GetMapping("/departments/hierarchy")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHierarchy(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Department hierarchy retrieved successfully",
                departmentService.getHierarchy()));
    }

    // ── 8. GET DEPARTMENTS DASHBOARD ─────────────────────────────────────────
    @GetMapping("/departments/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Department dashboard retrieved successfully",
                departmentService.getDashboard()));
    }

    // ── 9. GET DEPARTMENT EMPLOYEES (PAGINATED & FILTERED) ───────────────────
    @GetMapping("/departments/{id}/employees")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getEmployeesByDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return (ResponseEntity) departmentService.getDepartmentById(id)
                .<ResponseEntity<?>>map(dept -> {
                    List<Employee> employees = employeeService.getEmployeesByDepartment(dept.getName());

                    if (status != null && !status.isBlank()) {
                        employees = employees.stream()
                                .filter(e -> status.equalsIgnoreCase(e.getStatus()))
                                .toList();
                    }

                    int totalElements = employees.size();
                    int start = page * size;
                    List<Employee> sliced;
                    if (start >= totalElements) {
                        sliced = List.of();
                    } else {
                        int end = Math.min(start + size, totalElements);
                        sliced = employees.subList(start, end);
                    }

                    Map<String, Object> responseData = new LinkedHashMap<>();
                    responseData.put("content", sliced);
                    responseData.put("page", page);
                    responseData.put("size", size);
                    responseData.put("totalElements", totalElements);

                    return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully for department: " + dept.getName(), responseData));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Department not found with ID: " + id, "DEP_001")));
    }

    // ── 10. GET DEPARTMENT MANAGER ───────────────────────────────────────────
    @GetMapping("/departments/{id}/manager")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getManager(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return (ResponseEntity) departmentService.getManager(id)
                .<ResponseEntity<?>>map(manager -> {
                    Map<String, Object> details = new LinkedHashMap<>();
                    details.put("managerId", manager.getId());
                    details.put("fullName", manager.getFullName());
                    details.put("email", manager.getEmail());
                    details.put("designation", manager.getDesignation());
                    return ResponseEntity.ok(ApiResponse.success("Manager details retrieved successfully", details));
                })
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success("No manager assigned for department", null)));
    }

    // ── 11. PUT DEPARTMENT MANAGER ───────────────────────────────────────────
    @PutMapping("/departments/{id}/manager")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateManager(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Long> requestBody) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long managerId = requestBody.get("managerId");
        try {
            Department updated = departmentService.updateManager(id, managerId);
            return ResponseEntity.ok(ApiResponse.success("Manager assigned successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DEP_004"));
        }
    }

    // ── 12. POST TRANSFERS ───────────────────────────────────────────────────
    @PostMapping("/departments/transfers")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> transferEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody DepartmentTransferRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            DepartmentTransfer transfer = departmentService.transferEmployee(
                    request.getEmployeeId(),
                    request.getFromDepartmentId(),
                    request.getToDepartmentId(),
                    request.getEffectiveDate(),
                    request.getRemarks()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee transfer executed successfully", transfer));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DEP_005"));
        }
    }

    // ── 13. GET TRANSFERS ────────────────────────────────────────────────────
    @GetMapping("/departments/transfers")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<DepartmentTransfer>>> getAllTransfers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved successfully",
                departmentService.getAllTransfers()));
    }

    // ── 14. GET ANALYTICS: EMPLOYEE DISTRIBUTION ────────────────────────────
    @GetMapping("/departments/analytics/employee-distribution")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmployeeDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employee distribution retrieved successfully",
                departmentService.getEmployeeDistribution()));
    }

    // ── 15. GET ANALYTICS: BUDGET DISTRIBUTION ──────────────────────────────
    @GetMapping("/departments/analytics/budget-distribution")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBudgetDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Budget distribution retrieved successfully",
                departmentService.getBudgetDistribution()));
    }

    // ── 16. GET ANALYTICS: GROWTH ────────────────────────────────────────────
    @GetMapping("/departments/analytics/growth")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGrowth(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Growth data retrieved successfully",
                departmentService.getGrowth()));
    }

    // ── 17. GET ANALYTICS: HEADCOUNT TREND ───────────────────────────────────
    @GetMapping("/departments/analytics/headcount-trend")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHeadcountTrend(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Headcount trend retrieved successfully",
                departmentService.getHeadcountTrend()));
    }

    // ── 18. GET/PUT COST-CENTER ──────────────────────────────────────────────
    @GetMapping("/departments/{id}/cost-center")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCostCenter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            return ResponseEntity.ok(ApiResponse.success("Cost center retrieved successfully",
                    departmentService.getCostCenter(id)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DEP_001"));
        }
    }

    @PutMapping("/departments/{id}/cost-center")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateCostCenter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String costCenter = requestBody.get("costCenter");
        try {
            Department updated = departmentService.updateCostCenter(id, costCenter);
            return ResponseEntity.ok(ApiResponse.success("Cost center updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DEP_001"));
        }
    }

    // ── 19. GET/PUT BUDGET ───────────────────────────────────────────────────
    @GetMapping("/departments/{id}/budget")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBudget(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            return ResponseEntity.ok(ApiResponse.success("Budget details retrieved successfully",
                    departmentService.getBudget(id)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DEP_001"));
        }
    }

    @PutMapping("/departments/{id}/budget")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateBudget(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, java.math.BigDecimal> requestBody) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        java.math.BigDecimal allocated = requestBody.get("allocated");
        java.math.BigDecimal utilized = requestBody.get("utilized");
        try {
            Department updated = departmentService.updateBudgetFields(id, allocated, utilized);
            return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DEP_001"));
        }
    }

    // ── 20. GET REPORTS: HEADCOUNT ───────────────────────────────────────────
    @GetMapping("/departments/reports/headcount")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHeadcountReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Headcount report retrieved successfully",
                departmentService.getHeadcountReport()));
    }

    // ── 21. GET REPORTS: BUDGET-UTILIZATION ─────────────────────────────────
    @GetMapping("/departments/reports/budget-utilization")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBudgetUtilizationReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Budget utilization report retrieved successfully",
                departmentService.getBudgetUtilizationReport()));
    }

    // ── 22. GET REPORTS: EMPLOYEE-ALLOCATION ────────────────────────────────
    @GetMapping("/departments/reports/employee-allocation")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmployeeAllocationReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employee allocation report retrieved successfully",
                departmentService.getEmployeeAllocationReport()));
    }

    // ── 23. GET REPORTS: PERFORMANCE-SUMMARY ────────────────────────────────
    @GetMapping("/departments/reports/performance-summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPerformanceSummaryReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Performance summary report retrieved successfully",
                departmentService.getPerformanceSummaryReport()));
    }
}
