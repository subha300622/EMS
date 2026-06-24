package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.SalaryRevision;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Salary Management")
public class SalaryRevisionController {

    @Autowired
    private AppraisalService appraisalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    private boolean isFinanceOrManager(User user) {
        if (user == null) {
            return false;
        }
        boolean isManager = roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
        if (isManager) {
            return true;
        }

        if (roleService.hasRoleOrGreater(user, "FINANCE")) {
            return true;
        }

        return roleService.hasPermission(user.getWorkEmail(), "salary.manage")
                || roleService.hasPermission(user.getWorkEmail(), "payroll.manage")
                || roleService.hasPermission(user.getWorkEmail(), "reports.finance");
    }

    private boolean isHROrManager(User user) {
        if (user == null) {
            return false;
        }
        boolean isManager = roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
        if (isManager) {
            return true;
        }

        return roleService.hasRoleOrGreater(user, "HR");
    }

    // ── HELPERS & MAPPERS ────────────────────────────────────────────────────
    private String formatRevisionId(Long id) {
        if (id == null) return null;
        return "REV" + String.format("%03d", id);
    }

    private String formatLetterId(Long id) {
        if (id == null) return null;
        return "LETTER" + String.format("%03d", id);
    }

    private String formatDateTime(java.time.LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + "Z";
    }

    private Long parseRevisionId(String revisionId) {
        if (revisionId == null) {
            throw new IllegalArgumentException("Revision ID cannot be null");
        }
        if (revisionId.startsWith("REV")) {
            try {
                return Long.parseLong(revisionId.substring(3));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid revision ID format: " + revisionId);
            }
        }
        try {
            return Long.parseLong(revisionId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid revision ID format: " + revisionId);
        }
    }

    private Long parseEmployeeId(String employeeIdStr) {
        if (employeeIdStr == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        Optional<Employee> empOpt = employeeRepository.findByEmployeeId(employeeIdStr);
        if (empOpt.isPresent()) {
            return empOpt.get().getId();
        }
        try {
            return Long.parseLong(employeeIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Employee ID format: " + employeeIdStr);
        }
    }

    private boolean checkPerm(User user, String specificPermission, boolean managerOrFinanceAllowed) {
        if (user == null) {
            return false;
        }
        if (roleService.hasPermission(user.getWorkEmail(), specificPermission)
                || roleService.hasPermission(user.getWorkEmail(), "salary.manage")
                || roleService.hasPermission(user.getWorkEmail(), "payroll.manage")) {
            return true;
        }
        if (managerOrFinanceAllowed) {
            return isFinanceOrManager(user);
        }
        return false;
    }

    private boolean checkPermHROrManager(User user, String specificPermission) {
        if (user == null) {
            return false;
        }
        if (roleService.hasPermission(user.getWorkEmail(), specificPermission)
                || roleService.hasPermission(user.getWorkEmail(), "salary.manage")
                || roleService.hasPermission(user.getWorkEmail(), "payroll.manage")) {
            return true;
        }
        return isHROrManager(user);
    }

    private SalaryRevisionDetailedResponse mapToDetailedResponse(com.example.ems.appraisal.entity.Increment inc) {
        if (inc == null) return null;
        return new SalaryRevisionDetailedResponse(
            formatRevisionId(inc.getId()),
            inc.getEmployee() != null ? inc.getEmployee().getEmployeeId() : null,
            inc.getEmployee() != null ? inc.getEmployee().getFullName() : null,
            inc.getCurrentSalary(),
            inc.getIncrementPercentage(),
            inc.getIncrementAmount(),
            inc.getNewSalary(),
            inc.getEffectiveDate(),
            inc.getReason(),
            inc.getStatus(),
            formatDateTime(inc.getCreatedAt())
        );
    }

    private SalaryRevisionSummaryResponse mapToSummaryResponse(com.example.ems.appraisal.entity.Increment inc) {
        if (inc == null) return null;
        return new SalaryRevisionSummaryResponse(
            formatRevisionId(inc.getId()),
            inc.getEmployee() != null ? inc.getEmployee().getEmployeeId() : null,
            inc.getEmployee() != null ? inc.getEmployee().getFullName() : null,
            inc.getIncrementPercentage(),
            inc.getNewSalary(),
            inc.getEffectiveDate(),
            inc.getStatus()
        );
    }

    private SalaryRevisionUpdateResponse mapToUpdateResponse(com.example.ems.appraisal.entity.Increment inc) {
        if (inc == null) return null;
        return new SalaryRevisionUpdateResponse(
            formatRevisionId(inc.getId()),
            inc.getIncrementPercentage(),
            inc.getIncrementAmount(),
            inc.getNewSalary(),
            inc.getEffectiveDate(),
            inc.getStatus()
        );
    }

    private SalaryRevisionApproveResponse mapToApproveResponse(com.example.ems.appraisal.entity.Increment inc) {
        if (inc == null) return null;
        return new SalaryRevisionApproveResponse(
            formatRevisionId(inc.getId()),
            inc.getStatus(),
            formatDateTime(inc.getApprovedAt()),
            inc.getApprovedBy() != null ? inc.getApprovedBy().getFullName() : "HR Manager"
        );
    }

    private SalaryRevisionRejectResponse mapToRejectResponse(com.example.ems.appraisal.entity.Increment inc) {
        if (inc == null) return null;
        return new SalaryRevisionRejectResponse(
            formatRevisionId(inc.getId()),
            inc.getStatus(),
            formatDateTime(inc.getApprovedAt()),
            inc.getReason()
        );
    }

    private SalaryRevisionApplyResponse mapToApplyResponse(com.example.ems.appraisal.entity.Increment inc) {
        if (inc == null) return null;
        return new SalaryRevisionApplyResponse(
            formatRevisionId(inc.getId()),
            inc.getEmployee() != null ? inc.getEmployee().getEmployeeId() : null,
            inc.getCurrentSalary(),
            inc.getNewSalary(),
            formatDateTime(inc.getAppliedAt()),
            inc.getStatus()
        );
    }

    private SalaryRevisionHistoryResponse mapToHistoryResponse(SalaryRevision rev) {
        if (rev == null) return null;
        return new SalaryRevisionHistoryResponse(
            formatRevisionId(rev.getId()),
            rev.getEffectiveDate(),
            rev.getPreviousSalary(),
            rev.getNewSalary(),
            rev.getChangePercentage(),
            rev.getReason(),
            "APPLIED"
        );
    }

    private NewIncrementLetterResponse mapToNewLetterResponse(Long incrementId, IncrementLetterResponse letter) {
        if (letter == null) return null;
        return new NewIncrementLetterResponse(
            formatLetterId(incrementId),
            letter.getEmployeeId(),
            letter.getEmployeeName(),
            letter.getDesignation(),
            letter.getDepartment(),
            letter.getCurrentSalary(),
            letter.getIncrementPercentage(),
            letter.getIncrementAmount(),
            letter.getNewSalary(),
            letter.getEffectiveDate(),
            formatDateTime(LocalDateTime.now())
        );
    }

    // ── 1. CREATE INCREMENT REQUEST ──────────────────────────────────────────
    @PostMapping("/salary-revisions")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody NewIncrementRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermHROrManager(currentUser, "salary.revision.create")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        if (request.getEmployeeId() == null || request.getEmployeeId().trim().isEmpty()) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Employee ID is required", "VAL_001"));
        }
        if (request.getIncrementPercentage() == null) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Increment percentage is required", "VAL_001"));
        }
        if (request.getIncrementPercentage().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Increment percentage must be positive", "VAL_001"));
        }
        if (request.getEffectiveDate() == null) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Effective date is required", "VAL_001"));
        }

        try {
            com.example.ems.appraisal.entity.Increment created = appraisalService.createIncrement(request);
            SalaryRevisionDetailedResponse data = mapToDetailedResponse(created);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Salary increment request created successfully", data));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_001"));
        }
    }

    // ── 2. GET ALL SALARY REVISIONS ──────────────────────────────────────────
    @GetMapping("/salary-revisions")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSalaryRevisions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPerm(currentUser, "salary.revision.read", true)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance privileges.", "AUTH_002"));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<IncrementResponse> originalPage = appraisalService.getSalaryRevisions(status, pageable);
        List<SalaryRevisionSummaryResponse> summaries = originalPage.getContent().stream()
            .map(resp -> {
                Optional<com.example.ems.appraisal.entity.Increment> incOpt = appraisalService.getIncrementEntityById(resp.getId());
                return mapToSummaryResponse(incOpt.orElse(null));
            })
            .collect(Collectors.toList());

        java.util.Map<String, Object> paginationMap = new java.util.LinkedHashMap<>();
        paginationMap.put("page", originalPage.getNumber());
        paginationMap.put("size", originalPage.getSize());
        paginationMap.put("totalElements", originalPage.getTotalElements());
        paginationMap.put("totalPages", originalPage.getTotalPages());
        paginationMap.put("last", originalPage.isLast());

        java.util.Map<String, Object> responseData = new java.util.LinkedHashMap<>();
        responseData.put("content", summaries);
        responseData.put("pagination", paginationMap);
        
        return ResponseEntity.ok(ApiResponse.success("Salary revisions retrieved successfully", responseData));
    }

    // ── 3. GET SALARY REVISION DETAILS ───────────────────────────────────────
    @GetMapping("/salary-revisions/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<SalaryRevisionDetailedResponse>> getSalaryRevisionDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") String revisionId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long idVal;
        try {
            idVal = parseRevisionId(revisionId);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }

        Optional<com.example.ems.appraisal.entity.Increment> inc = appraisalService.getIncrementEntityById(idVal);
        if (inc.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + revisionId, "INC_002"));
        }

        com.example.ems.appraisal.entity.Increment response = inc.get();
        boolean isSelf = currentUser.getEmployeeId() != null 
                && response.getEmployee() != null
                && currentUser.getEmployeeId().equals(response.getEmployee().getEmployeeId());
        
        if (!isSelf && !checkPerm(currentUser, "salary.revision.read", true)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view other employees' revisions.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary revision details retrieved successfully", mapToDetailedResponse(response)));
    }

    // ── 4. UPDATE INCREMENT REQUEST ──────────────────────────────────────────
    @PutMapping("/salary-revisions/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<SalaryRevisionUpdateResponse>> updateIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") String revisionId,
            @Valid @RequestBody NewIncrementRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermHROrManager(currentUser, "salary.revision.update")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        Long idVal;
        try {
            idVal = parseRevisionId(revisionId);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }

        Optional<com.example.ems.appraisal.entity.Increment> updated = appraisalService.updateIncrement(
            idVal, request.getIncrementPercentage(), request.getEffectiveDate(), request.getReason()
        );
        if (updated.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + revisionId, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary increment request updated successfully", mapToUpdateResponse(updated.get())));
    }

    // ── 5. APPROVE SALARY REVISION ───────────────────────────────────────────
    @PatchMapping("/salary-revisions/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<SalaryRevisionApproveResponse>> approveIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") String revisionId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPerm(currentUser, "salary.revision.approve", true)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance/Manager approval privileges.", "AUTH_002"));
        }

        Long idVal;
        try {
            idVal = parseRevisionId(revisionId);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }

        Optional<com.example.ems.appraisal.entity.Increment> approved = appraisalService.approveIncrementEntity(idVal, currentUser.getWorkEmail());
        if (approved.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + revisionId, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary revision approved successfully", mapToApproveResponse(approved.get())));
    }

    // ── 6. REJECT SALARY REVISION ────────────────────────────────────────────
    @PatchMapping("/salary-revisions/{id}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<SalaryRevisionRejectResponse>> rejectIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") String revisionId,
            @Valid @RequestBody SalaryRevisionRejectRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPerm(currentUser, "salary.revision.approve", true)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance/Manager rejection privileges.", "AUTH_002"));
        }

        Long idVal;
        try {
            idVal = parseRevisionId(revisionId);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }

        Optional<com.example.ems.appraisal.entity.Increment> rejected = appraisalService.rejectIncrementEntity(idVal, currentUser.getWorkEmail(), request.getReason());
        if (rejected.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + revisionId, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary revision rejected successfully", mapToRejectResponse(rejected.get())));
    }

    // ── 7. APPLY SALARY REVISION ─────────────────────────────────────────────
    @PostMapping("/salary-revisions/{id}/apply")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<SalaryRevisionApplyResponse>> applyIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") String revisionId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPerm(currentUser, "salary.revision.apply", true)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance/Manager privileges to apply revision.", "AUTH_002"));
        }

        Long idVal;
        try {
            idVal = parseRevisionId(revisionId);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }

        Optional<com.example.ems.appraisal.entity.Increment> applied = appraisalService.applyIncrementEntity(idVal);
        if (applied.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + revisionId, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary revision applied successfully", mapToApplyResponse(applied.get())));
    }

    // ── 8. SALARY REVISION HISTORY ───────────────────────────────────────────
    @GetMapping("/employees/{employeeId}/salary-history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSalaryHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("employeeId") String employeeIdStr){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long empIdVal;
        try {
            empIdVal = parseEmployeeId(employeeIdStr);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }

        boolean isSelf = currentUser.getEmployeeId() != null 
                && currentUser.getEmployeeId().equals(employeeIdStr);
        if (!isSelf && !checkPerm(currentUser, "salary.revision.read", true)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view other employees' salary history.", "AUTH_002"));
        }

        List<SalaryRevision> history = appraisalService.getSalaryRevisionEntities(empIdVal);
        List<SalaryRevisionHistoryResponse> data = history.stream()
            .map(this::mapToHistoryResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Salary history retrieved successfully", data));
    }

    // ── 9. SALARY REVISION LETTER ────────────────────────────────────────────
    @GetMapping("/salary-revisions/{id}/letter")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<NewIncrementLetterResponse>> getIncrementLetter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") String revisionId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long idVal;
        try {
            idVal = parseRevisionId(revisionId);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }

        try {
            IncrementLetterResponse letter = appraisalService.getIncrementLetter(idVal);

            // Access check: allow if manager, or if letter belongs to employee
            if (!checkPerm(currentUser, "salary.revision.read", true)) {
                Optional<Employee> empOpt = employeeRepository.findByEmail(currentUser.getWorkEmail());
                if (empOpt.isEmpty() || !empOpt.get().getFullName().equals(letter.getEmployeeName())) {
                    return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.error("Access Denied: You cannot view other employees' letters.", "AUTH_002"));
                }
            }

            return ResponseEntity.ok(ApiResponse.success("Salary revision letter generated successfully", mapToNewLetterResponse(idVal, letter)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }
    }

    @Operation(summary = "Decoupled Payroll Execution from Finance Approved Appraisal")
    @PostMapping("/payroll-revisions/appraisals/{appraisalId}/execute")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> executePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrManager(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance or Admin permissions.", "AUTH_002"));
        }

        try {
            SalaryRevision rev = appraisalService.executePayrollDecoupled(appraisalId, currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Payroll executed successfully", rev));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAY_001"));
        }
    }

    @Operation(summary = "Payroll Execution Safe Retry API")
    @PostMapping("/payroll-revisions/appraisals/{appraisalId}/retry")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> retryPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrManager(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance or Admin permissions.", "AUTH_002"));
        }

        try {
            SalaryRevision rev = appraisalService.retryPayroll(appraisalId, currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Payroll executed/retrieved successfully", rev));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAY_002"));
        }
    }
}

