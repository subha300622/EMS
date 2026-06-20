package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AssetReturnConfirmRequest {

    @NotNull(message = "Return date is required")
    @Schema(example = "2026-06-19")
    private LocalDate returnDate;

    @NotBlank(message = "Condition is required")
    @Schema(example = "string")
    private String condition;

    @Schema(example = "string")
    private String remarks;

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
