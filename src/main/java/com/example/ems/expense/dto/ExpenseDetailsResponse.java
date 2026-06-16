package com.example.ems.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseDetailsResponse {
    private Long expenseId;
    private String expenseNumber;
    private EmployeeInfo employee;
    private String category;
    private String title;
    private String description;
    private LocalDate expenseDate;
    private BigDecimal amount;
    private String currency;
    private List<ReceiptInfo> receipts;
    private List<ApprovalStepInfo> approvalFlow;
    private PaymentInfo payment;

    public ExpenseDetailsResponse() {}

    public ExpenseDetailsResponse(Long expenseId, String expenseNumber, EmployeeInfo employee, String category, String title, String description, LocalDate expenseDate, BigDecimal amount, String currency, List<ReceiptInfo> receipts, List<ApprovalStepInfo> approvalFlow, PaymentInfo payment) {
        this.expenseId = expenseId;
        this.expenseNumber = expenseNumber;
        this.employee = employee;
        this.category = category;
        this.title = title;
        this.description = description;
        this.expenseDate = expenseDate;
        this.amount = amount;
        this.currency = currency;
        this.receipts = receipts;
        this.approvalFlow = approvalFlow;
        this.payment = payment;
    }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public String getExpenseNumber() { return expenseNumber; }
    public void setExpenseNumber(String expenseNumber) { this.expenseNumber = expenseNumber; }

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public List<ReceiptInfo> getReceipts() { return receipts; }
    public void setReceipts(List<ReceiptInfo> receipts) { this.receipts = receipts; }

    public List<ApprovalStepInfo> getApprovalFlow() { return approvalFlow; }
    public void setApprovalFlow(List<ApprovalStepInfo> approvalFlow) { this.approvalFlow = approvalFlow; }

    public PaymentInfo getPayment() { return payment; }
    public void setPayment(PaymentInfo payment) { this.payment = payment; }

    public static class EmployeeInfo {
        private Long employeeId;
        private String name;

        public EmployeeInfo() {}

        public EmployeeInfo(Long employeeId, String name) {
            this.employeeId = employeeId;
            this.name = name;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ReceiptInfo {
        private Long receiptId;
        private String fileName;
        private LocalDateTime uploadedAt;

        public ReceiptInfo() {}

        public ReceiptInfo(Long receiptId, String fileName, LocalDateTime uploadedAt) {
            this.receiptId = receiptId;
            this.fileName = fileName;
            this.uploadedAt = uploadedAt;
        }

        public Long getReceiptId() { return receiptId; }
        public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    public static class ApprovalStepInfo {
        private int level;
        private String approverRole;
        private String status;
        private LocalDateTime actionDate;
        private String comments;

        public ApprovalStepInfo() {}

        public ApprovalStepInfo(int level, String approverRole, String status, LocalDateTime actionDate, String comments) {
            this.level = level;
            this.approverRole = approverRole;
            this.status = status;
            this.actionDate = actionDate;
            this.comments = comments;
        }

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }

        public String getApproverRole() { return approverRole; }
        public void setApproverRole(String approverRole) { this.approverRole = approverRole; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getActionDate() { return actionDate; }
        public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }

        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    public static class PaymentInfo {
        private String status;
        private String expectedPaymentMonth;

        public PaymentInfo() {}

        public PaymentInfo(String status, String expectedPaymentMonth) {
            this.status = status;
            this.expectedPaymentMonth = expectedPaymentMonth;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getExpectedPaymentMonth() { return expectedPaymentMonth; }
        public void setExpectedPaymentMonth(String expectedPaymentMonth) { this.expectedPaymentMonth = expectedPaymentMonth; }
    }
}
