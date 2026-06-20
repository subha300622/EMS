package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinanceExpenseDetailsResponse {
    private Long expenseId;
    private EmployeeInfo employee;
    private String category;
    private String description;
    private BigDecimal amount;
    private String businessPurpose;
    private LocalDate submittedDate;
    private String status;
    private boolean receiptAttached;
    private String receiptUrl;

    public FinanceExpenseDetailsResponse() {}

    public FinanceExpenseDetailsResponse(Long expenseId, EmployeeInfo employee, String category, String description, BigDecimal amount, String businessPurpose, LocalDate submittedDate, String status, boolean receiptAttached, String receiptUrl) {
        this.expenseId = expenseId;
        this.employee = employee;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.businessPurpose = businessPurpose;
        this.submittedDate = submittedDate;
        this.status = status;
        this.receiptAttached = receiptAttached;
        this.receiptUrl = receiptUrl;
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public EmployeeInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeInfo employee) {
        this.employee = employee;
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

    public String getBusinessPurpose() {
        return businessPurpose;
    }

    public void setBusinessPurpose(String businessPurpose) {
        this.businessPurpose = businessPurpose;
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

    public boolean isReceiptAttached() {
        return receiptAttached;
    }

    public void setReceiptAttached(boolean receiptAttached) {
        this.receiptAttached = receiptAttached;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public static class EmployeeInfo {
        private Long id;
        private String name;
        private String department;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String name, String department) {
            this.id = id;
            this.name = name;
            this.department = department;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }
}
