package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class UpdateExpenseRequest {

    @NotBlank(message = "Expense title is required")
    @Schema(example = "Project Deliverables")
    private String title;

    @Schema(example = "Detailed description of the item")
    private String description;

    @NotNull(message = "Expense amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Schema(example = "5000.00")
    private BigDecimal amount;

    private List<Long> receiptIds;

    public UpdateExpenseRequest() {}

    public UpdateExpenseRequest(String title, String description, BigDecimal amount, List<Long> receiptIds) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.receiptIds = receiptIds;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Long> getReceiptIds() {
        return receiptIds;
    }

    public void setReceiptIds(List<Long> receiptIds) {
        this.receiptIds = receiptIds;
    }
}
