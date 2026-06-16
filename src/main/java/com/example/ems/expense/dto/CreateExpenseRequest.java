package com.example.ems.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateExpenseRequest {

    @NotBlank(message = "Expense category is required")
    private String category;

    @NotBlank(message = "Expense title is required")
    private String title;

    private String description;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Expense amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String currency = "INR";

    private String projectCode;

    private List<Long> receiptIds;

    public CreateExpenseRequest() {}

    public CreateExpenseRequest(String category, String title, String description, LocalDate expenseDate, BigDecimal amount, String currency, String projectCode, List<Long> receiptIds) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.expenseDate = expenseDate;
        this.amount = amount;
        this.currency = currency;
        this.projectCode = projectCode;
        this.receiptIds = receiptIds;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public List<Long> getReceiptIds() {
        return receiptIds;
    }

    public void setReceiptIds(List<Long> receiptIds) {
        this.receiptIds = receiptIds;
    }
}
