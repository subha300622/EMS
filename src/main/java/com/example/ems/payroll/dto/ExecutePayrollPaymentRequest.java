package com.example.ems.payroll.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for executing payroll payments")
public record ExecutePayrollPaymentRequest(
    @Schema(description = "Payment mode (e.g. NEFT, RTGS, IMPS, UPI)", example = "NEFT")
    String mode,

    @Schema(description = "Alternative payment mode property", example = "NEFT")
    String paymentMode,

    @Schema(description = "Simulation mode flag (e.g. SUCCESS, FAILURE)", example = "SUCCESS")
    String simulation
) {
    public String getEffectiveMode(String fallbackMode) {
        if (fallbackMode != null && !fallbackMode.isBlank()) {
            return fallbackMode;
        }
        if (paymentMode != null && !paymentMode.isBlank()) {
            return paymentMode;
        }
        if (mode != null && !mode.isBlank()) {
            return mode;
        }
        return "NEFT";
    }

    public String getEffectiveSimulation(String fallbackSimulation) {
        if (fallbackSimulation != null && !fallbackSimulation.isBlank()) {
            return fallbackSimulation;
        }
        return simulation;
    }
}
