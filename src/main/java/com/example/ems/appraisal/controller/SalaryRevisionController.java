package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.IncrementLetterResponse;
import com.example.ems.appraisal.dto.IncrementRequest;
import com.example.ems.appraisal.dto.IncrementResponse;
import com.example.ems.appraisal.dto.SalaryRevisionResponse;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

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

    // ── 1. CREATE INCREMENT REQUEST ──────────────────────────────────────────
    @PostMapping("/salary-revisions")
    public ResponseEntity<?> createIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody IncrementRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isHROrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            IncrementResponse response = appraisalService.createIncrement(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Increment request created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_001"));
        }
    }

    // ── 2. GET ALL SALARY REVISIONS ──────────────────────────────────────────
    @GetMapping("/salary-revisions")
    public ResponseEntity<?> getSalaryRevisions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance privileges.", "AUTH_002"));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<IncrementResponse> result = appraisalService.getSalaryRevisions(status, pageable);
        java.util.Map<String, Object> responseData = new java.util.LinkedHashMap<>();
        responseData.put("content", result.getContent());
        responseData.put("pageNumber", result.getNumber());
        responseData.put("pageSize", result.getSize());
        responseData.put("totalElements", result.getTotalElements());
        responseData.put("totalPages", result.getTotalPages());
        responseData.put("last", result.isLast());
        
        return ResponseEntity.ok(ApiResponse.success("Salary revisions retrieved successfully", responseData));
    }

    // ── 3. GET SALARY REVISION DETAILS ───────────────────────────────────────
    @GetMapping("/salary-revisions/{id}")
    public ResponseEntity<?> getSalaryRevisionDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Optional<IncrementResponse> inc = appraisalService.getIncrementById(id);
        if (inc.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + id, "INC_002"));
        }

        IncrementResponse response = inc.get();
        // Permission check: Finance/HR/Manager OR employee themselves
        boolean isSelf = currentUser.getEmployeeId() != null 
                && currentUser.getEmployeeId().equals(String.valueOf(response.getEmployeeId()));
        if (!isSelf && !isFinanceOrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view other employees' revisions.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary revision details retrieved successfully", response));
    }

    // ── 4. UPDATE INCREMENT REQUEST ──────────────────────────────────────────
    @PutMapping("/salary-revisions/{id}")
    public ResponseEntity<?> updateIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody IncrementRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isHROrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        Optional<IncrementResponse> updated = appraisalService.updateIncrement(id, request);
        if (updated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + id, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Increment request updated successfully", updated.get()));
    }

    // ── 5. APPROVE SALARY REVISION ───────────────────────────────────────────
    @PatchMapping("/salary-revisions/{id}/approve")
    public ResponseEntity<?> approveIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance/Manager approval privileges.", "AUTH_002"));
        }

        Optional<IncrementResponse> approved = appraisalService.approveIncrement(id, currentUser.getWorkEmail());
        if (approved.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + id, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Increment request approved successfully", approved.get()));
    }

    // ── 6. REJECT SALARY REVISION ────────────────────────────────────────────
    @PatchMapping("/salary-revisions/{id}/reject")
    public ResponseEntity<?> rejectIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance/Manager rejection privileges.", "AUTH_002"));
        }

        Optional<IncrementResponse> rejected = appraisalService.rejectIncrement(id, currentUser.getWorkEmail());
        if (rejected.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + id, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Increment request rejected successfully", rejected.get()));
    }

    // ── 7. APPLY SALARY REVISION ─────────────────────────────────────────────
    @PostMapping("/salary-revisions/{id}/apply")
    public ResponseEntity<?> applyIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance/Manager privileges to apply revision.", "AUTH_002"));
        }

        Optional<IncrementResponse> applied = appraisalService.applyIncrement(id);
        if (applied.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + id, "INC_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Increment applied and employee compensation updated successfully", applied.get()));
    }

    // ── 8. SALARY REVISION HISTORY ───────────────────────────────────────────
    @GetMapping("/employees/{employeeId}/salary-history")
    public ResponseEntity<?> getSalaryHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean isSelf = currentUser.getEmployeeId() != null 
                && currentUser.getEmployeeId().equals(String.valueOf(employeeId));
        if (!isSelf && !isFinanceOrManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view other employees' salary history.", "AUTH_002"));
        }

        List<SalaryRevisionResponse> history = appraisalService.getSalaryRevisions(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Salary history retrieved successfully", history));
    }

    // ── 9. SALARY REVISION LETTER ────────────────────────────────────────────
    @GetMapping("/salary-revisions/{id}/letter")
    public ResponseEntity<?> getIncrementLetter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            IncrementLetterResponse letter = appraisalService.getIncrementLetter(id);

            // Access check: allow if manager, or if letter belongs to employee
            if (!isFinanceOrManager(currentUser)) {
                // Find target employee
                Optional<Employee> empOpt = employeeRepository.findByEmail(currentUser.getWorkEmail());
                if (empOpt.isEmpty() || !empOpt.get().getFullName().equals(letter.getEmployeeName())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.error("Access Denied: You cannot view other employees' letters.", "AUTH_002"));
                }
            }

            return ResponseEntity.ok(ApiResponse.success("Increment revision letter generated successfully", letter));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }
    }
}
