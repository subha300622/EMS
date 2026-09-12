package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.audit.OnboardingAuditLogResponse;
import com.example.ems.onboarding.service.OnboardingAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding/{onboardingId}/audit-logs")
@CrossOrigin("*")
@Tag(name = "Onboarding Audit Logs")
public class OnboardingAuditLogController {

    @Autowired
    private OnboardingAuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get Onboarding Audit Logs (Read-Only Paginated)")
    public ResponseEntity<ApiResponse<Page<OnboardingAuditLogResponse>>> getAuditLogs(
            @PathVariable Long onboardingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OnboardingAuditLogResponse> response = auditLogService.getAuditLogs(onboardingId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }
}
