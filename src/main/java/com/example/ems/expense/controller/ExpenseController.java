package com.example.ems.expense.controller;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.ApprovalTask;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.repository.ApprovalTaskRepository;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.dto.ApproveExpenseRequest;
import com.example.ems.expense.dto.ExpenseDetailsResponse;
import com.example.ems.expense.dto.ExpenseRejectRequest;
import com.example.ems.expense.dto.MyExpenseItem;
import com.example.ems.expense.dto.MyExpenseListResponse;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.expense.service.MyExpenseService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/expenses")
@CrossOrigin("*")
@Tag(name = "Expense Management", description = "Domain APIs for Expense Claims and Approval Actions")
public class ExpenseController {

    @Autowired
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Autowired
    private ApprovalTaskRepository taskRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MyExpenseService myExpenseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    private Long resolveOrgId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                return jwtService.getOrgIdFromToken(token);
            }
        }
        return null;
    }

    private Employee resolveEmployee(User user) {
        if (user == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    @Operation(summary = "List Expenses", description = "Retrieves expenses for approval or review")
    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE_VIEW')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getExpenses(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        if (currentEmp == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Long orgId = resolveOrgId(authHeader);
        ApprovalStatus taskStatus = ApprovalStatus.PENDING;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            try {
                taskStatus = ApprovalStatus.valueOf(status.trim().toUpperCase());
            } catch (Exception ignored) {}
        }

        Pageable pageable = PageRequest.of(page, size);
        var taskPage = taskRepository.findInboxTasks(currentEmp.getId(), orgId, WorkflowType.EXPENSE_APPROVAL, taskStatus, pageable);

        List<MyExpenseItem> items = taskPage.getContent().stream().map(task -> {
            Long expId = Long.parseLong(task.getBusinessReferenceId());
            Optional<Expense> expOpt = expenseRepository.findById(expId);
            if (expOpt.isPresent()) {
                Expense exp = expOpt.get();
                MyExpenseItem.ActionInfo actions = new MyExpenseItem.ActionInfo(false, false, true);
                return new MyExpenseItem(
                        exp.getId(),
                        exp.getExpenseNumber(),
                        exp.getCategory() != null ? exp.getCategory().getCode() : "GENERAL",
                        exp.getTitle(),
                        exp.getExpenseDate(),
                        exp.getAmount(),
                        exp.getCurrency() != null ? exp.getCurrency() : "INR",
                        exp.getStatus(),
                        exp.getSubmittedAt(),
                        exp.getReimbursementStatus() != null ? exp.getReimbursementStatus() : "NOT_PAID",
                        actions
                );
            }
            return null;
        }).filter(item -> item != null).collect(Collectors.toList());

        MyExpenseListResponse.PaginationInfo pagInfo = new MyExpenseListResponse.PaginationInfo(
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.hasNext(),
                taskPage.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.success("Expenses retrieved successfully", new MyExpenseListResponse(items, pagInfo)));
    }

    @Operation(summary = "Get Expense Details", description = "Retrieves details of a specific expense claim")
    @GetMapping("/{expenseId}")
    @PreAuthorize("hasAuthority('EXPENSE_VIEW')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getExpenseDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        if (currentEmp == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        ExpenseDetailsResponse details = myExpenseService.getExpenseDetails(expenseId, currentEmp);
        return ResponseEntity.ok(ApiResponse.success("Expense details retrieved successfully", details));
    }

    @Operation(summary = "Approve Expense Claim", description = "Approves an expense claim")
    @PostMapping("/{expenseId}/approve")
    @PreAuthorize("hasAuthority('EXPENSE_APPROVE')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @RequestBody(required = false) ApproveExpenseRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        if (currentEmp == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        List<ApprovalTask> tasks = taskRepository.findActiveTasksForBusinessRef(WorkflowType.EXPENSE_APPROVAL, "EXPENSE", expenseId.toString());
        Optional<ApprovalTask> assignedTaskOpt = tasks.stream()
                .filter(t -> t.getApprover() != null && t.getApprover().getId().equals(currentEmp.getId()) && t.getStatus() == ApprovalStatus.PENDING)
                .findFirst();

        String taskId = assignedTaskOpt.map(ApprovalTask::getApprovalTaskId)
                .orElseGet(() -> tasks.isEmpty() ? null : tasks.get(0).getApprovalTaskId());

        if (taskId == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("No active approval task found for expense ID: " + expenseId, "EXP_400"));
        }

        String comment = (request != null && request.getComments() != null) ? request.getComments() : "Approved";
        var result = approvalWorkflowEngineService.approveTask(currentUser, taskId, comment);

        Expense exp = expenseRepository.findById(expenseId).orElse(null);
        if (exp != null && "PENDING_MANAGER_APPROVAL".equals(exp.getStatus())) {
            exp.setStatus("PENDING_FINANCE_APPROVAL");
            expenseRepository.save(exp);
        }

        return ResponseEntity.ok(ApiResponse.success("Expense claim approved successfully", result));
    }

    @Operation(summary = "Reject Expense Claim", description = "Rejects an expense claim")
    @PostMapping("/{expenseId}/reject")
    @PreAuthorize("hasAuthority('EXPENSE_REJECT')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody ExpenseRejectRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        if (currentEmp == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        List<ApprovalTask> tasks = taskRepository.findActiveTasksForBusinessRef(WorkflowType.EXPENSE_APPROVAL, "EXPENSE", expenseId.toString());
        Optional<ApprovalTask> assignedTaskOpt = tasks.stream()
                .filter(t -> t.getApprover() != null && t.getApprover().getId().equals(currentEmp.getId()) && t.getStatus() == ApprovalStatus.PENDING)
                .findFirst();

        String taskId = assignedTaskOpt.map(ApprovalTask::getApprovalTaskId)
                .orElseGet(() -> tasks.isEmpty() ? null : tasks.get(0).getApprovalTaskId());

        if (taskId == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("No active approval task found for expense ID: " + expenseId, "EXP_400"));
        }

        String reason = (request != null && request.getReason() != null) ? request.getReason() : "Rejected";
        var result = approvalWorkflowEngineService.rejectTask(currentUser, taskId, reason);

        Expense exp = expenseRepository.findById(expenseId).orElse(null);
        if (exp != null) {
            exp.setStatus("REJECTED");
            exp.setRejectionReason(reason);
            expenseRepository.save(exp);
        }

        return ResponseEntity.ok(ApiResponse.success("Expense claim rejected successfully", result));
    }

    @Operation(summary = "Send Back Expense Claim", description = "Requests changes on an expense claim")
    @PostMapping("/{expenseId}/send-back")
    @PreAuthorize("hasAuthority('EXPENSE_APPROVE')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> sendBackExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody ExpenseRejectRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Employee currentEmp = resolveEmployee(currentUser);
        if (currentEmp == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        List<ApprovalTask> tasks = taskRepository.findActiveTasksForBusinessRef(WorkflowType.EXPENSE_APPROVAL, "EXPENSE", expenseId.toString());
        Optional<ApprovalTask> assignedTaskOpt = tasks.stream()
                .filter(t -> t.getApprover() != null && t.getApprover().getId().equals(currentEmp.getId()) && t.getStatus() == ApprovalStatus.PENDING)
                .findFirst();

        String taskId = assignedTaskOpt.map(ApprovalTask::getApprovalTaskId)
                .orElseGet(() -> tasks.isEmpty() ? null : tasks.get(0).getApprovalTaskId());

        if (taskId == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("No active approval task found for expense ID: " + expenseId, "EXP_400"));
        }

        String comments = (request != null && request.getReason() != null) ? request.getReason() : "Changes requested";
        var result = approvalWorkflowEngineService.requestChanges(currentUser, taskId, comments);

        Expense exp = expenseRepository.findById(expenseId).orElse(null);
        if (exp != null) {
            exp.setStatus("CHANGES_REQUESTED");
            exp.setSendBackReason(comments);
            expenseRepository.save(exp);
        }

        return ResponseEntity.ok(ApiResponse.success("Expense claim sent back for changes", result));
    }

    @Operation(summary = "Reimburse Expense Claim", description = "Marks expense as reimbursed")
    @PostMapping("/{expenseId}/reimburse")
    @PreAuthorize("hasAuthority('EXPENSE_REIMBURSE')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> reimburseExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Expense exp = expenseRepository.findById(expenseId).orElse(null);
        if (exp == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Expense claim not found.", "EXP_404"));
        }

        exp.setReimbursementStatus("PAID");
        exp.setStatus("REIMBURSED");
        expenseRepository.save(exp);

        return ResponseEntity.ok(ApiResponse.success("Expense claim reimbursed successfully", exp));
    }
}
