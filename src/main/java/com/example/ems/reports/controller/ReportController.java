package com.example.ems.reports.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Reports & Analytics")
public class ReportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    // In-memory state storage
    private static final Map<Long, Map<String, Object>> scheduledReports = new ConcurrentHashMap<>();
    private static final AtomicLong scheduleIdGenerator = new AtomicLong(1);

    private static final Map<String, Map<String, Object>> exports = new ConcurrentHashMap<>();

    private static final List<Map<String, Object>> reportHistory = new ArrayList<>();

    static {
        // Initialize mock report history
        Map<String, Object> h1 = new LinkedHashMap<>();
        h1.put("id", 1L);
        h1.put("reportName", "Payroll Report");
        h1.put("generatedBy", "Admin");
        h1.put("generatedAt", "2026-06-19T10:00:00");
        reportHistory.add(h1);

        // Initialize a default schedule
        Map<String, Object> s1 = new LinkedHashMap<>();
        s1.put("id", 1L);
        s1.put("reportType", "PAYROLL");
        s1.put("frequency", "MONTHLY");
        s1.put("emailRecipients", List.of("finance@company.com"));
        s1.put("status", "ACTIVE");
        scheduledReports.put(1L, s1);
        scheduleIdGenerator.set(2);
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

    // ── 1. REPORT DASHBOARD SUMMARY ──────────────────────────────────────────
    @Operation(summary = "Get Dashboard Summary metrics")
    @GetMapping("/reports/dashboard-summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false, defaultValue = "MONTH") String period,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false, defaultValue = "ALL") String category) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("employeeCost", 2840000);
        summary.put("departmentCost", 1420000);
        summary.put("taxLiabilities", 420000);
        summary.put("assetValue", 48000000L);
        summary.put("pendingExpenses", 42800);
        summary.put("netDisbursement", 2420000);

        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", summary));
    }

    // ── 2. PAYROLL COST TREND CHART ──────────────────────────────────────────
    @Operation(summary = "Get Payroll Cost Trend data for charts")
    @GetMapping("/reports/payroll-cost-trend")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollCostTrend(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("labels", List.of("May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr"));
        trend.put("values", List.of(2400000, 2450000, 2440000, 2560000, 2540000, 2670000, 2640000, 2750000, 2740000, 2790000, 2840000, 2800000));

        return ResponseEntity.ok(ApiResponse.success("Payroll cost trend retrieved successfully", trend));
    }

    // ── 3. DEPARTMENT COST DISTRIBUTION ──────────────────────────────────────
    @Operation(summary = "Get Department Cost Distribution for donut chart")
    @GetMapping("/reports/department-cost-distribution")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDepartmentCostDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<Map<String, Object>> depts = new ArrayList<>();
        depts.add(createDeptCost("Engineering", 1278000, 45));
        depts.add(createDeptCost("Sales", 568000, 20));
        depts.add(createDeptCost("Marketing", 426000, 15));
        depts.add(createDeptCost("Operations", 340800, 12));
        depts.add(createDeptCost("HR", 227200, 8));

        Map<String, Object> distribution = new LinkedHashMap<>();
        distribution.put("totalCost", 2840000);
        distribution.put("departments", depts);

        return ResponseEntity.ok(ApiResponse.success("Department cost distribution retrieved successfully", distribution));
    }

    private Map<String, Object> createDeptCost(String name, int cost, int percentage) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("department", name);
        map.put("cost", cost);
        map.put("percentage", percentage);
        return map;
    }

    // ── 4. PAYROLL REPORTS TAB ───────────────────────────────────────────────
    @Operation(summary = "Get Payroll Report metrics")
    @GetMapping("/reports/payroll")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollReportTab(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("salarySummary", 2840000);
        report.put("grossPay", 3400000);
        report.put("netPay", 2840000);
        report.put("deductions", 560000);
        report.put("payrollRunStatus", "COMPLETED");

        return ResponseEntity.ok(ApiResponse.success("Payroll report retrieved successfully", report));
    }

    // ── 5. EXPENSE REPORTS TAB ───────────────────────────────────────────────
    @Operation(summary = "Get Expense Report metrics")
    @GetMapping("/reports/expenses")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExpenseReportTab(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<Map<String, Object>> categorySpend = new ArrayList<>();
        categorySpend.add(Map.of("category", "Travel", "spend", 25000));
        categorySpend.add(Map.of("category", "Meals", "spend", 10800));
        categorySpend.add(Map.of("category", "Office Supplies", "spend", 7000));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("approvedExpenses", 150000);
        report.put("pendingExpenses", 42800);
        report.put("rejectedExpenses", 12000);
        report.put("categoryWiseSpend", categorySpend);

        return ResponseEntity.ok(ApiResponse.success("Expense report retrieved successfully", report));
    }

    // ── 6. TAX REPORTS TAB ───────────────────────────────────────────────────
    @Operation(summary = "Get Tax Report metrics")
    @GetMapping("/reports/tax")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaxReportTab(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("tds", 250000);
        report.put("pf", 120000);
        report.put("esi", 30000);
        report.put("professionalTax", 20000);
        report.put("gst", 0);

        return ResponseEntity.ok(ApiResponse.success("Tax report retrieved successfully", report));
    }

    // ── 7. ASSET REPORTS TAB ──────────────────────────────────────────────────
    @Operation(summary = "Get Asset Report metrics")
    @GetMapping("/reports/assets")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssetReportTab(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("assetValue", 48000000L);
        report.put("depreciation", 2400000);
        report.put("assetAllocation", 38000000L);
        report.put("assetMaintenanceCost", 150000);

        return ResponseEntity.ok(ApiResponse.success("Asset report retrieved successfully", report));
    }

    // ── 8. CUSTOM REPORT BUILDER ─────────────────────────────────────────────
    @Operation(summary = "Build a custom analytical report")
    @PostMapping("/reports/custom")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> buildCustomReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String name = (String) body.getOrDefault("name", "Custom Report");
        String module = (String) body.getOrDefault("module", "PAYROLL");
        List<String> columns = (List<String>) body.get("columns");

        Map<String, Object> mockRow1 = new LinkedHashMap<>();
        mockRow1.put("employeeName", "John Doe");
        mockRow1.put("department", "Engineering");
        mockRow1.put("grossSalary", 100000);
        mockRow1.put("netSalary", 85000);

        Map<String, Object> mockRow2 = new LinkedHashMap<>();
        mockRow2.put("employeeName", "Jane Smith");
        mockRow2.put("department", "Engineering");
        mockRow2.put("grossSalary", 120000);
        mockRow2.put("netSalary", 102000);

        Map<String, Object> customReport = new LinkedHashMap<>();
        customReport.put("name", name);
        customReport.put("module", module);
        customReport.put("columns", columns);
        customReport.put("data", List.of(mockRow1, mockRow2));

        // Add to history
        Map<String, Object> historyItem = new LinkedHashMap<>();
        historyItem.put("id", (long) (reportHistory.size() + 1));
        historyItem.put("reportName", name);
        historyItem.put("generatedBy", currentUser.getFullName());
        historyItem.put("generatedAt", LocalDateTime.now().toString());
        reportHistory.add(historyItem);

        return ResponseEntity.ok(ApiResponse.success("Custom report built successfully", customReport));
    }

    // ── 9. REPORT HISTORY ────────────────────────────────────────────────────
    @Operation(summary = "Get list of previously generated reports")
    @GetMapping("/reports/history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReportHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> history = new LinkedHashMap<>();
        history.put("content", reportHistory);

        return ResponseEntity.ok(ApiResponse.success("Report history retrieved successfully", history));
    }

    // ── 10. EXPORT REPORT ────────────────────────────────────────────────────
    @Operation(summary = "Request a report export file")
    @PostMapping("/reports/export")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String reportType = (String) body.getOrDefault("reportType", "PAYROLL");
        String format = (String) body.getOrDefault("format", "EXCEL");

        String exportId = "exp_" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> expData = new LinkedHashMap<>();
        expData.put("exportId", exportId);
        expData.put("reportType", reportType);
        expData.put("format", format);
        expData.put("status", "COMPLETED");
        expData.put("downloadUrl", "/api/v1/reports/export/" + exportId);

        exports.put(exportId, expData);

        return ResponseEntity.ok(ApiResponse.success("Export generated successfully", expData));
    }

    // ── 11. DOWNLOAD EXPORT ──────────────────────────────────────────────────
    @Operation(summary = "Download previously requested report file")
    @GetMapping("/reports/export/{exportId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<?> downloadExport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String exportId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> exp = exports.get(exportId);
        if (exp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Export file not found or expired with ID: " + exportId, "REP_004"));
        }

        String format = (String) exp.get("format");
        String filename = "report_" + exportId + ("CSV".equalsIgnoreCase(format) ? ".csv" : ".xlsx");
        byte[] simulatedBytes = ("Simulated " + format + " report file bytes").getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(simulatedBytes.length);

        return new ResponseEntity<>(simulatedBytes, headers, HttpStatus.OK);
    }

    // ── 12. SCHEDULE REPORT ──────────────────────────────────────────────────
    @Operation(summary = "Create a recurring scheduled report")
    @PostMapping("/reports/schedules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> scheduleReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        long id = scheduleIdGenerator.getAndIncrement();
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("id", id);
        schedule.put("reportType", body.get("reportType"));
        schedule.put("frequency", body.get("frequency"));
        schedule.put("emailRecipients", body.get("emailRecipients"));
        schedule.put("status", "ACTIVE");

        scheduledReports.put(id, schedule);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report schedule created successfully", schedule));
    }

    // ── 13. SCHEDULED REPORTS CRUD ───────────────────────────────────────────
    @Operation(summary = "List all active report schedules")
    @GetMapping("/reports/schedules")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSchedules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<Map<String, Object>> list = new ArrayList<>(scheduledReports.values());
        return ResponseEntity.ok(ApiResponse.success("Report schedules retrieved successfully", list));
    }

    @Operation(summary = "Update an existing report schedule")
    @PutMapping("/reports/schedules/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSchedule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> schedule = scheduledReports.get(id);
        if (schedule == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Report schedule not found with ID: " + id, "REP_005"));
        }

        if (body.containsKey("reportType")) schedule.put("reportType", body.get("reportType"));
        if (body.containsKey("frequency")) schedule.put("frequency", body.get("frequency"));
        if (body.containsKey("emailRecipients")) schedule.put("emailRecipients", body.get("emailRecipients"));
        if (body.containsKey("status")) schedule.put("status", body.get("status"));

        return ResponseEntity.ok(ApiResponse.success("Report schedule updated successfully", schedule));
    }

    @Operation(summary = "Delete an existing report schedule")
    @DeleteMapping("/reports/schedules/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSchedule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> removed = scheduledReports.remove(id);
        if (removed == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Report schedule not found with ID: " + id, "REP_005"));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Schedule deleted successfully");

        return ResponseEntity.ok(ApiResponse.success("Report schedule deleted successfully", res));
    }

    // ── 14. DROPDOWNS ────────────────────────────────────────────────────────
    @Operation(summary = "Get list of branches for dropdown selection")
    @GetMapping("/branches/dropdown")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBranchesDropdown(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<Map<String, Object>> branches = List.of(
                Map.of("id", 1L, "name", "Headquarters"),
                Map.of("id", 2L, "name", "Branch Office")
        );

        return ResponseEntity.ok(ApiResponse.success("Branches dropdown retrieved successfully", branches));
    }

    @Operation(summary = "Get available report categories")
    @GetMapping("/reports/categories")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<String>>> getCategories(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<String> categories = List.of("PAYROLL", "EXPENSES", "TAX", "ASSETS");
        return ResponseEntity.ok(ApiResponse.success("Report categories retrieved successfully", categories));
    }

    @Operation(summary = "Get available report periods")
    @GetMapping("/reports/periods")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<String>>> getPeriods(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<String> periods = List.of("MONTH", "QUARTER", "YEAR");
        return ResponseEntity.ok(ApiResponse.success("Report periods retrieved successfully", periods));
    }
}
