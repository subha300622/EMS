package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.PaymentConfigRequest;
import com.example.ems.payroll.dto.PaymentConfigResponse;
import com.example.ems.payroll.service.PaymentConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/payroll/payment-config", "/api/v1/payment-configurations"})
public class PaymentConfigurationController {

    private final PaymentConfigurationService configService;

    public PaymentConfigurationController(PaymentConfigurationService configService) {
        this.configService = configService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentConfigResponse>> saveConfiguration(
            @Valid @RequestBody PaymentConfigRequest request) {
        PaymentConfigResponse response = configService.saveConfiguration(request);
        return ResponseEntity.ok(ApiResponse.success("Payment configuration saved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaymentConfigResponse>> getConfiguration() {
        PaymentConfigResponse response = configService.getConfiguration();
        return ResponseEntity.ok(ApiResponse.success("Payment configuration retrieved successfully", response));
    }
}
