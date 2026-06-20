package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinanceExpenseListItem {
    private Long expenseId;
    private Long employeeId;
    private String employeeName;
    private String department;
    private String category;
    private String description;
    private BigDecimal amount;
    private boolean receiptAttached;
    private LocalDate submittedDate;
    private String status;

    public FinanceExpenseListItem() {}

    public FinanceExpenseListItem(Long expenseId, Long employeeId, String employeeName, String department, String category, String description, BigDecimal amount, boolean receiptAttached, LocalDate submittedDate, String status) {
        this.expenseId = expenseId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.receiptAttached = receiptAttached;
        this.submittedDate = submittedDate;
        this.status = status;
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isReceiptAttached() {
        return receiptAttached;
    }

    public void setReceiptAttached(boolean receiptAttached) {
        this.receiptAttached = receiptAttached;
    }

    public LocalDate getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDate submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
