package com.example.ems.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Calculated CTC breakup response")
public record CtcBreakupResponse(
        @Schema(description = "Annual Cost to Company", example = "1200000.00")
        BigDecimal annualCtc,

        @Schema(description = "Monthly Cost to Company", example = "100000.00")
        BigDecimal monthlyCtc,

        @Schema(description = "Basic Salary component", example = "50000.00")
        BigDecimal basicSalary,

        @Schema(description = "House Rent Allowance component", example = "20000.00")
        BigDecimal hra,

        @Schema(description = "Special Allowances component", example = "30000.00")
        BigDecimal allowances,

        @Schema(description = "Provident Fund deduction", example = "6000.00")
        BigDecimal providentFund,

        @Schema(description = "Professional Tax deduction", example = "200.00")
        BigDecimal professionalTax,

        @Schema(description = "Income Tax (TDS) deduction", example = "10000.00")
        BigDecimal incomeTax,

        @Schema(description = "Estimated Net Take-Home Pay", example = "83800.00")
        BigDecimal netPay
) {}
