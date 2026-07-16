package com.example.ems.employee.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.DepartmentTransferRequest;
import com.example.ems.employee.entity.DepartmentTransfer;
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

        // ── 6. GET DEPARTMENTS DROPDOWN ──────────────────────────────────────────
        @Operation(summary = "Get Departments Dropdown", description = "Retrieves a lightweight list of departments (ID and name) for UI dropdown selections.")
        @GetMapping("/departments/dropdown")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Department Hierarchy", description = "Retrieves the organizational reporting structure of company departments.")
        @GetMapping("/departments/hierarchy")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Department Dashboard", description = "Retrieves a department dashboard summary with headcounts and budget statistics.")
        @GetMapping("/departments/dashboard")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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

        // ── 12. POST TRANSFERS ───────────────────────────────────────────────────
        @Operation(summary = "Transfer Employee Department", description = "Records and executes an employee transfer between departments with an effective date.")
        @PostMapping("/departments/transfers")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
                                        request.getRemarks());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success("Employee transfer executed successfully", transfer));
                } catch (IllegalArgumentException e) {
                        return (ResponseEntity) ResponseEntity.badRequest()
                                        .body(ErrorResponse.error(e.getMessage(), "DEP_005"));
                }
        }

        // ── 13. GET TRANSFERS ────────────────────────────────────────────────────
        @Operation(summary = "Get All Department Transfers", description = "Retrieves a history log of all employee department transfers.")
        @GetMapping("/departments/transfers")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Employee Distribution Analytics", description = "Retrieves employee count distribution across departments for analytical charts.")
        @GetMapping("/departments/analytics/employee-distribution")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Budget Distribution Analytics", description = "Retrieves total budget allocation statistics mapped across departments.")
        @GetMapping("/departments/analytics/budget-distribution")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Department Growth Analytics", description = "Retrieves growth metrics and recruitment trends by department.")
        @GetMapping("/departments/analytics/growth")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Headcount Trend Analytics", description = "Retrieves headcount trend timelines filtered by departments.")
        @GetMapping("/departments/analytics/headcount-trend")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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

        // ── 20. GET REPORTS: HEADCOUNT ───────────────────────────────────────────
        @Operation(summary = "Get Headcount Report", description = "Generates a headcount distribution summary report across departments.")
        @GetMapping("/departments/reports/headcount")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Budget Utilization Report", description = "Generates a department budget utilization report showing allocated vs spent funds.")
        @GetMapping("/departments/reports/budget-utilization")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Employee Allocation Report", description = "Generates an employee placement and allocation report by department.")
        @GetMapping("/departments/reports/employee-allocation")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Operation(summary = "Get Department Performance Report", description = "Generates a consolidated performance appraisal summary report grouped by department.")
        @GetMapping("/departments/reports/performance-summary")
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
