package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.service.RazorpayXWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/webhooks/razorpayx", "/api/v1/integrations/razorpayx/webhooks/payouts"})
public class RazorpayXWebhookController {

    private final RazorpayXWebhookService webhookService;

    public RazorpayXWebhookController(RazorpayXWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        webhookService.processWebhook(rawBody, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully", "OK"));
    }
}

