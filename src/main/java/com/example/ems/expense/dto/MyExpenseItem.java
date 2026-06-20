package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MyExpenseItem {
    @Schema(example = "1")
    private Long expenseId;
    @Schema(example = "string")
    private String expenseNumber;
    @Schema(example = "string")
    private String category;
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "2026-06-19")
    private LocalDate expenseDate;
    @Schema(example = "5000.00")
    private BigDecimal amount;
    @Schema(example = "string")
    private String currency;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime submittedAt;
    @Schema(example = "ACTIVE")
    private String reimbursementStatus;
    private ActionInfo actions;

    public MyExpenseItem() {}

    public MyExpenseItem(Long expenseId, String expenseNumber, String category, String title, LocalDate expenseDate, BigDecimal amount, String currency, String status, LocalDateTime submittedAt, String reimbursementStatus, ActionInfo actions) {
        this.expenseId = expenseId;
        this.expenseNumber = expenseNumber;
        this.category = category;
        this.title = title;
        this.expenseDate = expenseDate;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reimbursementStatus = reimbursementStatus;
        this.actions = actions;
    }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public String getExpenseNumber() { return expenseNumber; }
    public void setExpenseNumber(String expenseNumber) { this.expenseNumber = expenseNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getReimbursementStatus() { return reimbursementStatus; }
    public void setReimbursementStatus(String reimbursementStatus) { this.reimbursementStatus = reimbursementStatus; }

    public ActionInfo getActions() { return actions; }
    public void setActions(ActionInfo actions) { this.actions = actions; }

    public static class ActionInfo {
        @Schema(example = "true")
        private boolean canEdit;
        @Schema(example = "true")
        private boolean canWithdraw;
        @Schema(example = "true")
        private boolean canView;

        public ActionInfo() {}

        public ActionInfo(boolean canEdit, boolean canWithdraw, boolean canView) {
            this.canEdit = canEdit;
            this.canWithdraw = canWithdraw;
            this.canView = canView;
        }

        public boolean isCanEdit() { return canEdit; }
        public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

        public boolean isCanWithdraw() { return canWithdraw; }
        public void setCanWithdraw(boolean canWithdraw) { this.canWithdraw = canWithdraw; }

        public boolean isCanView() { return canView; }
        public void setCanView(boolean canView) { this.canView = canView; }
    }
}
