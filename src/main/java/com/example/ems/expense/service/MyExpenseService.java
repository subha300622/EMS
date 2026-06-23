package com.example.ems.expense.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.dto.*;
import com.example.ems.expense.entity.*;
import com.example.ems.expense.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MyExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private MyExpenseReceiptRepository receiptRepository;

    @Autowired
    private MyExpensePolicyRepository policyRepository;

    @Autowired
    private MyExpenseApprovalStepRepository approvalStepRepository;

    @Autowired
    private MyExpenseTimelineEventRepository timelineEventRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.example.ems.auth.repository.UserRepository userRepository;

    @EventListener(ContextRefreshedEvent.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedDatabase() {
        // 1. Seed Categories if needed (enrich existing or create new)
        ExpenseCategory travelCat = categoryRepository.findByName("Travel")
                .orElseGet(() -> {
                    ExpenseCategory c = new ExpenseCategory();
                    c.setName("Travel");
                    c.setDescription("Travel expenses");
                    return categoryRepository.save(c);
                });
        travelCat.setCode("TRAVEL");
        travelCat.setMaxLimit(BigDecimal.valueOf(10000.00));
        travelCat.setRequiresReceipt(true);
        categoryRepository.save(travelCat);

        ExpenseCategory mealsCat = categoryRepository.findByName("Meals")
                .orElseGet(() -> {
                    ExpenseCategory c = new ExpenseCategory();
                    c.setName("Meals");
                    c.setDescription("Meals and dining expenses");
                    return categoryRepository.save(c);
                });
        mealsCat.setCode("MEALS");
        mealsCat.setMaxLimit(BigDecimal.valueOf(2000.00));
        mealsCat.setRequiresReceipt(true);
        categoryRepository.save(mealsCat);

        ExpenseCategory internetCat = categoryRepository.findByName("Internet")
                .orElseGet(() -> {
                    ExpenseCategory c = new ExpenseCategory();
                    c.setName("Internet");
                    c.setDescription("Internet and phone allowance");
                    return categoryRepository.save(c);
                });
        internetCat.setCode("INTERNET");
        internetCat.setMaxLimit(BigDecimal.valueOf(1500.00));
        internetCat.setRequiresReceipt(false);
        categoryRepository.save(internetCat);

        System.out.println("Seeded Expense Categories.");

        // 2. Seed Policy if empty
        if (policyRepository.count() == 0) {
            policyRepository.save(new MyExpensePolicy(
                    "Travel Reimbursement Policy",
                    "Travel expenses require valid receipts and manager approval. Standard local travel and dining allowances are capped per category limits.",
                    LocalDate.of(2025, 1, 1),
                    "1.0"
            ));
            System.out.println("Seeded Expense Policies.");
        }

        // 3. Seed 24 Mock claims for employee if empty
        Optional<Employee> mockEmployeeOpt = employeeRepository.findAll().stream()
                .filter(e -> {
                    com.example.ems.auth.entity.User u = userRepository.findByWorkEmail(e.getEmail()).orElse(null);
                    return u != null && u.getRole() != null && "EMPLOYEE".equalsIgnoreCase(u.getRole().getName());
                })
                .findFirst();
        if (mockEmployeeOpt.isPresent()) {
            Employee emp = mockEmployeeOpt.get();
            List<Expense> existingExpenses = expenseRepository.findByEmployeeId(emp.getId());
            if (existingExpenses.isEmpty() && expenseRepository.findByExpenseNumber("EXP-2026-0001").isEmpty()) {
                int expNumCounter = 1;

                // 15 Reimbursed claims (3600.00 each -> total 54000.00, REIMBURSED status, PAID reimbursement)
                for (int i = 0; i < 15; i++) {
                    Expense exp = new Expense();
                    exp.setEmployee(emp);
                    exp.setTitle("Reimbursed Client Visit " + (i + 1));
                    exp.setAmount(BigDecimal.valueOf(3600.00));
                    exp.setExpenseDate(LocalDate.now().minusMonths(1).minusDays(i));
                    exp.setStatus("REIMBURSED");
                    exp.setReimbursementStatus("PAID");
                    exp.setCurrency("INR");
                    exp.setProjectCode("PRJ-101");
                    exp.setDescription("Travel fare reimbursement for client meetings");
                    exp.setCategory(travelCat);
                    exp.setExpenseNumber(String.format("EXP-2026-%04d", expNumCounter++));
                    exp.setSubmittedAt(LocalDate.now().minusMonths(1).minusDays(i).atStartOfDay());
                    exp.setExpectedPaymentMonth(LocalDate.now().minusMonths(1).getMonth().name() + " 2026");
                    expenseRepository.save(exp);

                    // Seed timeline events
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "CREATED", emp.getFullName()));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "SUBMITTED", emp.getFullName()));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "MANAGER_APPROVED", "Manager"));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "PAYMENT_PROCESSED", "Finance"));

                    // Seed approval step
                    approvalStepRepository.save(new MyExpenseApprovalStep(exp, 1, "MANAGER", "APPROVED", LocalDateTime.now().minusMonths(1), "Approved travel"));
                    approvalStepRepository.save(new MyExpenseApprovalStep(exp, 2, "FINANCE", "APPROVED", LocalDateTime.now().minusMonths(1), "Reimbursement processed"));
                }

                // 3 Approved but not reimbursed claims (3400.00 each -> total 10200.00, APPROVED status, NOT_PAID reimbursement)
                for (int i = 0; i < 3; i++) {
                    Expense exp = new Expense();
                    exp.setEmployee(emp);
                    exp.setTitle("Approved Meals " + (i + 1));
                    exp.setAmount(BigDecimal.valueOf(3400.00));
                    exp.setExpenseDate(LocalDate.now().minusDays(i + 5));
                    exp.setStatus("APPROVED");
                    exp.setReimbursementStatus("NOT_PAID");
                    exp.setCurrency("INR");
                    exp.setProjectCode("PRJ-101");
                    exp.setDescription("Client team lunch");
                    exp.setCategory(mealsCat);
                    exp.setExpenseNumber(String.format("EXP-2026-%04d", expNumCounter++));
                    exp.setSubmittedAt(LocalDate.now().minusDays(i + 5).atStartOfDay());
                    exp.setExpectedPaymentMonth(LocalDate.now().plusMonths(1).getMonth().name() + " 2026");
                    expenseRepository.save(exp);

                    // Seed timeline events
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "CREATED", emp.getFullName()));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "SUBMITTED", emp.getFullName()));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "MANAGER_APPROVED", "Manager"));

                    // Seed approval steps
                    approvalStepRepository.save(new MyExpenseApprovalStep(exp, 1, "MANAGER", "APPROVED", LocalDateTime.now().minusDays(4), "Approved meals"));
                    approvalStepRepository.save(new MyExpenseApprovalStep(exp, 2, "FINANCE", "PENDING", null, null));
                }

                // 3 Pending claims (total 12400.00 -> 2 of 4000.00, 1 of 4400.00, PENDING_MANAGER_APPROVAL)
                BigDecimal[] pendingAmounts = {BigDecimal.valueOf(4000.00), BigDecimal.valueOf(4000.00), BigDecimal.valueOf(4400.00)};
                for (int i = 0; i < 3; i++) {
                    Expense exp = new Expense();
                    exp.setEmployee(emp);
                    exp.setTitle("Pending Claim " + (i + 1));
                    exp.setAmount(pendingAmounts[i]);
                    exp.setExpenseDate(LocalDate.now().minusDays(i + 1));
                    exp.setStatus("PENDING_MANAGER_APPROVAL");
                    exp.setReimbursementStatus("NOT_PAID");
                    exp.setCurrency("INR");
                    exp.setProjectCode("PRJ-101");
                    exp.setDescription("Broadband reimbursement and travel expense");
                    exp.setCategory(travelCat);
                    exp.setExpenseNumber(String.format("EXP-2026-%04d", expNumCounter++));
                    exp.setSubmittedAt(LocalDateTime.now().minusDays(i + 1));
                    expenseRepository.save(exp);

                    // Seed timeline events
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "CREATED", emp.getFullName()));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "SUBMITTED", emp.getFullName()));

                    // Seed approval steps
                    approvalStepRepository.save(new MyExpenseApprovalStep(exp, 1, "MANAGER", "PENDING", null, null));
                }

                // 2 Rejected claims (4000.00 each -> total 8000.00, REJECTED status)
                for (int i = 0; i < 2; i++) {
                    Expense exp = new Expense();
                    exp.setEmployee(emp);
                    exp.setTitle("Rejected Internet Bill " + (i + 1));
                    exp.setAmount(BigDecimal.valueOf(4000.00));
                    exp.setExpenseDate(LocalDate.now().minusDays(i + 10));
                    exp.setStatus("REJECTED");
                    exp.setReimbursementStatus("NOT_PAID");
                    exp.setCurrency("INR");
                    exp.setProjectCode("PRJ-101");
                    exp.setDescription("Duplicate receipt submitted");
                    exp.setRejectionReason("Duplicate receipt submitted");
                    exp.setCategory(internetCat);
                    exp.setExpenseNumber(String.format("EXP-2026-%04d", expNumCounter++));
                    exp.setSubmittedAt(LocalDate.now().minusDays(i + 10).atStartOfDay());
                    expenseRepository.save(exp);

                    // Seed timeline events
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "CREATED", emp.getFullName()));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "SUBMITTED", emp.getFullName()));
                    timelineEventRepository.save(new MyExpenseTimelineEvent(exp, "REJECTED", "Manager"));

                    // Seed approval steps
                    approvalStepRepository.save(new MyExpenseApprovalStep(exp, 1, "MANAGER", "REJECTED", LocalDateTime.now().minusDays(9), "Invalid receipt/limit exceeded"));
                }

                // 1 Draft claim (1000.00, DRAFT status)
                Expense draftExp = new Expense();
                draftExp.setEmployee(emp);
                draftExp.setTitle("Draft Broadband Bill");
                draftExp.setAmount(BigDecimal.valueOf(1000.00));
                draftExp.setExpenseDate(LocalDate.now());
                draftExp.setStatus("DRAFT");
                draftExp.setReimbursementStatus("NOT_PAID");
                draftExp.setCurrency("INR");
                draftExp.setProjectCode("PRJ-101");
                draftExp.setDescription("Internet bill draft saving");
                draftExp.setCategory(internetCat);
                draftExp.setExpenseNumber(String.format("EXP-2026-%04d", expNumCounter++));
                expenseRepository.save(draftExp);

                // Seed timeline events
                timelineEventRepository.save(new MyExpenseTimelineEvent(draftExp, "CREATED", emp.getFullName()));

                System.out.println("Seeded 24 Mock Expense Claims for " + emp.getEmail() + ".");
            }
        }
    }

    @Transactional(readOnly = true)
    public MyExpenseDashboardResponse getDashboard(Employee employee) {
        List<Expense> expenses = expenseRepository.findByEmployeeId(employee.getId());

        int totalClaims = expenses.size();
        int pendingApproval = 0;
        int approvedClaims = 0;
        int rejectedClaims = 0;
        int reimbursedClaims = 0;

        BigDecimal totalClaimAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal approvedAmount = BigDecimal.ZERO;
        BigDecimal reimbursedAmount = BigDecimal.ZERO;

        for (Expense exp : expenses) {
            BigDecimal amt = exp.getAmount() != null ? exp.getAmount() : BigDecimal.ZERO;
            totalClaimAmount = totalClaimAmount.add(amt);

            String status = exp.getStatus();
            if ("PENDING".equals(status) || "SUBMITTED".equals(status) || "PENDING_MANAGER_APPROVAL".equals(status) || "PENDING_FINANCE_APPROVAL".equals(status)) {
                pendingApproval++;
                pendingAmount = pendingAmount.add(amt);
            } else if ("APPROVED".equals(status) || "PAYMENT_PROCESSING".equals(status) || "REIMBURSED".equals(status)) {
                approvedClaims++;
                approvedAmount = approvedAmount.add(amt);
                if ("REIMBURSED".equals(status) || "PAID".equals(exp.getReimbursementStatus())) {
                    reimbursedClaims++;
                    reimbursedAmount = reimbursedAmount.add(amt);
                }
            } else if ("REJECTED".equals(status)) {
                rejectedClaims++;
            }
        }

        MyExpenseDashboardResponse.EmployeeInfo empInfo = new MyExpenseDashboardResponse.EmployeeInfo(
                employee.getId(),
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getDepartment()
        );

        MyExpenseDashboardResponse.SummaryInfo summary = new MyExpenseDashboardResponse.SummaryInfo(
                totalClaims,
                pendingApproval,
                approvedClaims,
                rejectedClaims,
                reimbursedClaims,
                totalClaimAmount,
                pendingAmount,
                approvedAmount,
                reimbursedAmount,
                "INR"
        );

        return new MyExpenseDashboardResponse(empInfo, summary, "FY-2025-26", LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public MyExpenseListResponse getMyExpenses(Employee employee, String status, String category, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        com.example.ems.expense.entity.ExpenseStatus statusEnum = (status != null && !status.trim().isEmpty())
                ? com.example.ems.expense.entity.ExpenseStatus.valueOf(status.trim().toUpperCase())
                : null;
        Page<Expense> page = expenseRepository.findByFilters(employee.getId(), statusEnum, category, fromDate, toDate, pageable);

        List<MyExpenseItem> items = page.getContent().stream()
                .map(exp -> {
                    boolean canEdit = "DRAFT".equals(exp.getStatus()) || "REJECTED".equals(exp.getStatus());
                    boolean canWithdraw = "SUBMITTED".equals(exp.getStatus()) 
                            || "PENDING_MANAGER_APPROVAL".equals(exp.getStatus()) 
                            || "PENDING_FINANCE_APPROVAL".equals(exp.getStatus()) 
                            || "PENDING".equals(exp.getStatus());

                    MyExpenseItem.ActionInfo actions = new MyExpenseItem.ActionInfo(canEdit, canWithdraw, true);

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
                })
                .collect(Collectors.toList());

        MyExpenseListResponse.PaginationInfo pagInfo = new MyExpenseListResponse.PaginationInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new MyExpenseListResponse(items, pagInfo);
    }

    @Transactional(readOnly = true)
    public ExpenseDetailsResponse getExpenseDetails(Long expenseId, Employee employee) {
        Expense exp = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        List<ExpenseDetailsResponse.ReceiptInfo> receipts = receiptRepository.findAll().stream()
                .filter(r -> r.getExpense() != null && r.getExpense().getId().equals(expenseId))
                .map(r -> new ExpenseDetailsResponse.ReceiptInfo(r.getId(), r.getFileName(), r.getUploadedAt()))
                .collect(Collectors.toList());

        List<ExpenseDetailsResponse.ApprovalStepInfo> steps = approvalStepRepository.findByExpenseIdOrderByLevelAsc(expenseId).stream()
                .map(s -> new ExpenseDetailsResponse.ApprovalStepInfo(s.getLevel(), s.getApproverRole(), s.getStatus(), s.getActionDate(), s.getComments()))
                .collect(Collectors.toList());

        ExpenseDetailsResponse.PaymentInfo payment = new ExpenseDetailsResponse.PaymentInfo(
                "REIMBURSED".equals(exp.getStatus()) ? "PAID" : "PENDING",
                exp.getExpectedPaymentMonth() != null ? exp.getExpectedPaymentMonth() : "May 2026"
        );

        return new ExpenseDetailsResponse(
                exp.getId(),
                exp.getExpenseNumber(),
                new ExpenseDetailsResponse.EmployeeInfo(employee.getId(), employee.getFullName()),
                exp.getCategory() != null ? exp.getCategory().getCode() : "GENERAL",
                exp.getTitle(),
                exp.getDescription(),
                exp.getExpenseDate(),
                exp.getAmount(),
                exp.getCurrency() != null ? exp.getCurrency() : "INR",
                receipts,
                steps,
                payment
        );
    }

    public CreateExpenseResponse createExpense(CreateExpenseRequest request, Employee employee) {
        ExpenseCategory category = categoryRepository.findByCode(request.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found with code: " + request.getCategory()));

        Expense exp = new Expense();
        exp.setEmployee(employee);
        exp.setTitle(request.getTitle());
        exp.setDescription(request.getDescription());
        exp.setExpenseDate(request.getExpenseDate());
        exp.setAmount(request.getAmount());
        exp.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        exp.setProjectCode(request.getProjectCode());
        exp.setCategory(category);
        exp.setStatus("PENDING_MANAGER_APPROVAL");
        exp.setReimbursementStatus("NOT_PAID");
        exp.setExpenseNumber("EXP-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        exp.setSubmittedAt(LocalDateTime.now());
        exp.setExpectedPaymentMonth(LocalDate.now().plusMonths(1).getMonth().name() + " 2026");

        Expense saved = expenseRepository.save(exp);

        // Bind receipts if provided
        if (request.getReceiptIds() != null && !request.getReceiptIds().isEmpty()) {
            for (Long rId : request.getReceiptIds()) {
                receiptRepository.findById(rId).ifPresent(receipt -> {
                    receipt.setExpense(saved);
                    receiptRepository.save(receipt);
                });
            }
        }

        // Timeline Audit
        timelineEventRepository.save(new MyExpenseTimelineEvent(saved, "CREATED", employee.getFullName()));
        timelineEventRepository.save(new MyExpenseTimelineEvent(saved, "SUBMITTED", employee.getFullName()));

        // Approval Flow level 1
        approvalStepRepository.save(new MyExpenseApprovalStep(saved, 1, "MANAGER", "PENDING", null, null));

        return new CreateExpenseResponse(
                saved.getId(),
                saved.getExpenseNumber(),
                saved.getStatus(),
                saved.getSubmittedAt(),
                "Expense claim submitted successfully"
        );
    }

    public UpdateExpenseResponse updateExpense(Long expenseId, UpdateExpenseRequest request, Employee employee) {
        Expense exp = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        if (!"DRAFT".equals(exp.getStatus()) && !"REJECTED".equals(exp.getStatus())) {
            throw new IllegalStateException("Expense claims can only be updated when in DRAFT or REJECTED status.");
        }

        exp.setTitle(request.getTitle());
        exp.setDescription(request.getDescription());
        exp.setAmount(request.getAmount());
        exp.setUpdatedAt(LocalDateTime.now());

        // Update receipts mapping
        if (request.getReceiptIds() != null && !request.getReceiptIds().isEmpty()) {
            for (Long rId : request.getReceiptIds()) {
                receiptRepository.findById(rId).ifPresent(receipt -> {
                    receipt.setExpense(exp);
                    receiptRepository.save(receipt);
                });
            }
        }

        Expense saved = expenseRepository.save(exp);

        // Timeline Event
        timelineEventRepository.save(new MyExpenseTimelineEvent(saved, "UPDATED", employee.getFullName()));

        return new UpdateExpenseResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getUpdatedAt(),
                "Expense updated successfully"
        );
    }

    public WithdrawExpenseResponse withdrawExpense(Long expenseId, WithdrawExpenseRequest request, Employee employee) {
        Expense exp = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        boolean canWithdraw = "SUBMITTED".equals(exp.getStatus()) 
                || "PENDING_MANAGER_APPROVAL".equals(exp.getStatus()) 
                || "PENDING_FINANCE_APPROVAL".equals(exp.getStatus()) 
                || "PENDING".equals(exp.getStatus());

        if (!canWithdraw) {
            throw new IllegalStateException("Expense claims can only be withdrawn before approval.");
        }

        exp.setStatus("WITHDRAWN");
        exp.setUpdatedAt(LocalDateTime.now());
        Expense saved = expenseRepository.save(exp);

        // Timeline Event
        timelineEventRepository.save(new MyExpenseTimelineEvent(saved, "WITHDRAWN", employee.getFullName() + " (" + request.getReason() + ")"));

        return new WithdrawExpenseResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getUpdatedAt(),
                "Expense claim withdrawn successfully"
        );
    }

    public UploadReceiptResponse uploadReceipt(MultipartFile file, String receiptType, Employee employee) throws Exception {
        byte[] fileData = file.getBytes();
        MyExpenseReceipt receipt = new MyExpenseReceipt(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                fileData,
                receiptType != null ? receiptType : "RECEIPT",
                employee
        );

        MyExpenseReceipt saved = receiptRepository.save(receipt);

        return new UploadReceiptResponse(
                saved.getId(),
                saved.getFileName(),
                saved.getFileType(),
                saved.getFileSize(),
                saved.getUploadedAt()
        );
    }

    @Transactional(readOnly = true)
    public MyExpenseReceipt downloadReceipt(Long receiptId) {
        return receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found with ID: " + receiptId));
    }

    @Transactional(readOnly = true)
    public ExpenseTimelineResponse getExpenseTimeline(Long expenseId, Employee employee) {
        Expense exp = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        List<MyExpenseTimelineEvent> events = timelineEventRepository.findByExpenseIdOrderByDateAsc(expenseId);
        List<ExpenseTimelineResponse.TimelineEventItem> items = events.stream()
                .map(e -> new ExpenseTimelineResponse.TimelineEventItem(e.getEvent(), e.getPerformedBy(), e.getDate()))
                .collect(Collectors.toList());

        return new ExpenseTimelineResponse(exp.getId(), items);
    }

    @Transactional(readOnly = true)
    public ExpenseCategoriesResponse getCategories() {
        List<ExpenseCategoriesResponse.CategoryItem> items = categoryRepository.findAll().stream()
                .map(c -> new ExpenseCategoriesResponse.CategoryItem(c.getCode(), c.getName(), c.getMaxLimit(), c.isRequiresReceipt()))
                .collect(Collectors.toList());
        return new ExpenseCategoriesResponse(items);
    }

    @Transactional(readOnly = true)
    public ExpensePoliciesResponse getPolicies() {
        List<ExpensePoliciesResponse.PolicyItem> items = policyRepository.findAll().stream()
                .map(p -> new ExpensePoliciesResponse.PolicyItem(p.getId(), p.getTitle(), p.getDescription(), p.getEffectiveFrom(), p.getVersion()))
                .collect(Collectors.toList());
        return new ExpensePoliciesResponse(items);
    }
}
